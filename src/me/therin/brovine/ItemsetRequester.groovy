package me.therin.brovine

import com.google.gson.Gson
import me.therin.mining.itemsets.data.Basket
import me.therin.mining.itemsets.data.BasketIterator
import me.therin.mining.itemsets.ItemsetGenerator

/**
 * ItemsetRequester.java
 * 
 * TODO: Class description
 *
 * @author tcirwin
 * @version TODO: the version
 * @date March 6, 2013
 */
class ItemsetRequester<Item> implements Runnable {
    static final int PORT = 8100
    static final String DONE = "done"
    static final String BAD_GENERATOR = "Itemset generator %s was not found"
    static final String BAD_ITERATOR = "Iterator %s was not found"
    static final String USAGE = "java ItemsetRequester <generator-name> <iterator>"

    private static String defaultGen
    private static Map<String, ItemsetGenerator> genMap = new HashMap<>()
    private Socket client
    private ItemsetGenerator<Item> generator

    private static enum ReqType { GET, UPDATE, SET }

    private static enum Result { SUCCESS, FAILURE }
    private static enum Reason {
        GET, UPDATE, SET, INVALID_SUP, INVALID_TYPE, INVALID_FORMAT,
        INVALID_GEN, INVALID_ITER, INVALID_CLASS
    }

    private class Request<Item> {
        protected final ReqType type
        private final double supPct
        private List<Basket<Item>> baskets

        protected Request(ReqType type) { this.type = type }

        public Request(String type, String supPct, List<String> data)
        throws NumberFormatException, IllegalArgumentException {
            baskets = new ArrayList<Basket<Item>>()
            this.supPct = Double.parseDouble(supPct)
            this.type = ReqType.valueOf(ReqType, type.toUpperCase())

            for (String gn : data)
                baskets.add(new Basket<Item>(gn))
        }
    }

    private class SetRequest extends Request {
        private final String gen
        private final String iter

        public SetRequest(String gen, String iter) {
            super(ReqType.SET)
            this.gen = gen
            this.iter = iter
        }
    }

    private class Response<Item> {
        public final Result res
        public final Reason reason
        public final String message
        public final long time
        public Map<List<Item>, Integer> data

        public Response(Result res, Reason r, String message) {
            this.res = res
            this.reason = r
            this.message = message
            this.data = null
            this.time = 0
        }

        public Response(Result res, Reason r, def data, long time) {
            this.res = res
            this.reason = r
            this.message = ""
            this.data = data
            this.time = time
        }

        @Override
        public String toString() { return new Gson().toJson(this) }
    }

    public ItemsetRequester(Socket client) {
        this.client = client
        getGenerator(defaultGen)
    }

    @Override
    void run() {
        String input = ""
        Response output
        def reader = new Scanner(client.getInputStream())
        def writer = new PrintWriter(client.getOutputStream())
        System.err.println("Client connected.")

        while ((input = reader.nextLine()) != null) {
            try {
                if (input.equalsIgnoreCase(DONE))
                    break
                output = doRequest(parseRequest(input))
            }
            catch (NumberFormatException e) {
                output = new Response(Result.FAILURE, Reason.INVALID_SUP,
                        "The minSup must be a double between 0 and 1.")
            }
            catch (IllegalArgumentException e) {
                output = new Response(Result.FAILURE, Reason.INVALID_TYPE,
                        "Request not valid: invalid request type.")
            }
            catch (UnsupportedOperationException e) {
                output = new Response(Result.FAILURE, Reason.INVALID_FORMAT,
                        "Request not valid: must contain at least 2 words.")
            }
            catch (ClassNotFoundException e) {
                output = new Response(Result.FAILURE, Reason.INVALID_CLASS,
                        "Request not valid: class `$e.message` does not exist.")
            }

            writer.println(output)
            writer.flush()
        }

        if (generator != null)
            generator.reset()

        writer.close()
        reader.close()
        client.close()
    }

    public static boolean listen() throws IOException {
        boolean done = false
        def server = new ServerSocket(PORT)

        System.err.printf("Server started on port %d; waiting for clients.\n", PORT)

        while (!done) {
            new Thread(new ItemsetRequester(server.accept())).start()
        }

        server.close()
        return true
    }

    public static void setDefaultGenerator(String gen, String iter) {
        def constr = Class.forName(gen).getConstructor([BasketIterator] as Class[])
        def bask = Class.forName(iter).newInstance()
        def gener = constr.newInstance(bask)


        if (!(gener instanceof ItemsetGenerator)) {
            System.err.printf(BAD_GENERATOR + "\n", gen)
            System.exit(-1)
        }
        if (!(bask instanceof BasketIterator)) {
            System.err.printf(BAD_ITERATOR + "\n", iter)
            System.exit(-1)
        }

        defaultGen = gen + iter
        genMap.put(defaultGen, (ItemsetGenerator) gener)
    }

    public Response doRequest(Request req) {
        if (req.type == ReqType.GET) {
            def start = System.currentTimeMillis()
            def data = generator.getFrequentItemsets(req.supPct)
            return new Response(Result.SUCCESS, Reason.GET, data, System.currentTimeMillis() - start)
        }
        else if (req.type == ReqType.SET) {
            SetRequest sq = (SetRequest) req
            return getGenerator(sq.gen, sq.iter)
        }
        //TODO: add support for live database updates
        /*else if (req.type == ReqType.UPDATE) {
            return new Response((generator.update())
              ? Result.SUCCESS : Result.FAILURE, "", null)
        }*/
    }

    public Request parseRequest(String data) throws Exception {
        String[] strs = data.split(" ")
        def arr = new ArrayList<String>()

        if (strs.length == 3 && strs[0].equalsIgnoreCase("set")) {
            return new SetRequest(strs[1], strs[2])
        }
        else if (strs.length >= 2) {
            for (int i = 2; i < strs.length; i++)
                arr.add(strs[i])

            return new Request(strs[0], strs[1], arr)
        }
        else throw new UnsupportedOperationException()
    }

    public Response getGenerator(String key) { return getGenerator(key, "") }

    public synchronized Response getGenerator(String gen, String iter) {
        // Try to load the generator from the map; if it doesn't yet exist we
        // create it
        if ((generator = genMap.get(gen + iter)) == null) {
            def constr = Class.forName(gen).getConstructor([BasketIterator] as Class[])
            def bask = Class.forName(iter).newInstance()
            def gener = constr.newInstance(bask)

            if (!(gener instanceof ItemsetGenerator)) {
                System.err.printf(BAD_GENERATOR + "\n", gen)
                return new Response(Result.FAILURE, Reason.INVALID_GEN,
                  "Classname given is not derived from ItemsetGenerator.")
            }
            if (!(bask instanceof BasketIterator)) {
                System.err.printf(BAD_ITERATOR + "\n", iter)
                return new Response(Result.FAILURE, Reason.INVALID_ITER,
                  "Classname given is not derived from BasketIterator.")
            }

            // Save the new generator to the map
            generator = (ItemsetGenerator) gener
            genMap.put(gen + iter, generator)
        }

        return new Response(Result.SUCCESS, Reason.SET, null, 0)
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(USAGE)
            System.exit(-1)
        }

        try {
            ItemsetRequester.setDefaultGenerator(args[0], args[1])
            ItemsetRequester.listen()
        }
        catch (IOException e) {
            e.printStackTrace()
            System.exit(-1)
        }
    }
}