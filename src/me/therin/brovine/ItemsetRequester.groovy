package me.therin.brovine

import com.google.gson.Gson
import me.therin.mining.itemsets.data.Basket
import me.therin.mining.itemsets.data.BasketIterator
import me.therin.mining.itemsets.ItemsetGenerator
import java.text.SimpleDateFormat

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
    static final String INFORM = " Command `usage` returns API spec."
    static final String LOG_FORMAT = "%s [%s] %s\n"
    static final def DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss")
    
    static final String API_USAGE = """
`get [minSup:decimal:0-1] [maxSup:decimal:0-1]`
    Returns: `{
        'res': "(SUCCESS | FAILURE)":string,
        'reason': "Reason for failure if FAILURE, request type if SUCCESS.":string,
        'message': "Explanation of failure iff 'res' == 'FAILURE'.":string,
        'time': "integer indicating the time it took to find itemsets":integer,
        'data': "map with the itemsets and their frequency counts; ex: [[one, two]: 138]":map
    }'

    Returns a list of the itemsets between the min and max support values.
    'time' is always 0 on FAILURE.
    `maxSup` must be less than `minSup`, obviously. Both are decimal values
    indicating the percent support an itemset must have to be included.

`set [BasketIterator] [ItemsetGenerator]`
    Returns: same as Get request except with no 'data' attribute; 'time'
    will always be 0

    If the request is successful, 'res' will be set to SUCCESS and 'reason'
    will be "SET". Any subsequent queries by client will use the
    BasketIterator and ItemsetGenerator specified.
    Both Set values must be fully-qualified class names.
    """

    private static String defaultGen
    private static String defaultIter
    private static Map<String, List<ItemsetGenerator>> genMap = new HashMap<>()
    private Socket client
    private UUID uuid
    private ItemsetGenerator<Item> generator

    private static enum ReqType { GET, UPDATE, SET, USAGE }

    private static enum Result { SUCCESS, FAILURE }
    private static enum Reason {
        GET, UPDATE, SET, USAGE, INVALID_SUP, INVALID_TYPE, INVALID_FORMAT,
        INVALID_GEN, INVALID_ITER, INVALID_CLASS
    }

    private class Request<Item> {
        protected final ReqType type
        private final double supPct
        private final double maxPct
        private List<Basket<Item>> baskets

        protected Request(ReqType type) { this.type = type }

        public Request(String type, String supPct, String maxPct, List<String> data)
        throws NumberFormatException, IllegalArgumentException {
            baskets = new ArrayList<Basket<Item>>()
            this.supPct = Double.parseDouble(supPct)
            this.maxPct = Double.parseDouble(maxPct)
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
        this.uuid = UUID.randomUUID()
        getGenerator(defaultGen, defaultIter)
    }

    /**
     * Usage:
     *
     * Get request: `get [minSup:decimal:0-1] [maxSup:decimal:0-1]`
     * Returns: `{
     *    'res': "(SUCCESS | FAILURE)":string,
     *    'reason': "Reason for failure if FAILURE, request type if SUCCESS.":string,
     *    'message': "Explanation of failure iff 'res' == 'FAILURE'.":string,
     *    'time': "integer indicating the time it took to find itemsets":integer,
     *    'data': "map with the itemsets and their frequency counts; ex: [[one, two]: 138]":map
     * }'
     * Returns a list of the itemsets between the min and max support values.
     * 'time' is always 0 on FAILURE.
     * `maxSup` must be less than `minSup`, obviously. Both are decimal values
     * indicating the percent support an itemset must have to be included.
     *
     * Set request: `set [BasketIterator] [ItemsetGenerator]`
     * Returns: same as Get request except with no 'data' attribute; 'time'
     * will always be 0
     * If the request is successful, 'res' will be set to SUCCESS and 'reason'
     * will be "SET". Any subsequent queries by client will use the
     * BasketIterator and ItemsetGenerator specified.
     *
     * Both Set values must be fully-qualified class names.
     */
    @Override
    void run() {
        String input = ""
        Response output
        def reader = new Scanner(client.getInputStream())
        def writer = new PrintWriter(client.getOutputStream())
        log("Client connected.")

        try {
            while (reader.hasNextLine() && (input = reader.nextLine()) != null) {
                log(input)

                try {
                    if (input.equalsIgnoreCase(DONE))
                        break
                    else if (input.equalsIgnoreCase("usage"))
                        output = new Response(Result.SUCCESS, Reason.USAGE, API_USAGE)
                    else
                        output = doRequest(parseRequest(input))
                }
                catch (NumberFormatException e) {
                    output = new Response(Result.FAILURE, Reason.INVALID_SUP,
                            "The minSup must be a double between 0 and 1." + INFORM)
                }
                catch (IllegalArgumentException e) {
                    output = new Response(Result.FAILURE, Reason.INVALID_TYPE,
                            "Request not valid: invalid request type." + INFORM)
                }
                catch (UnsupportedOperationException e) {
                    output = new Response(Result.FAILURE, Reason.INVALID_FORMAT,
                            "Request not valid: must contain at least 2 words." + INFORM)
                }
                catch (ClassNotFoundException e) {
                    output = new Response(Result.FAILURE, Reason.INVALID_CLASS,
                            "Request not valid: class `$e.message` does not exist." + INFORM)
                }

                log(output.toString())
                writer.println(output)
                writer.flush()
            }
        }
        catch (Exception e) {
            log("Exception encountered. Message: " + e.message)
            e.printStackTrace()
        }
        finally {
            if (generator != null) {
                generator.release()
                generator.reset()
            }

            log("Client disconnected.")
            writer.close()
            reader.close()
            client.close()
        }
    }

    public static boolean listen() throws IOException {
        boolean done = false
        def server = new ServerSocket(PORT)

        log_msg(String.format("Server started on port %d; waiting for clients.\n", PORT))

        while (!done) {
            def req = null

            try {
                req = new ItemsetRequester(server.accept())
                new Thread(req).start()
            }
            catch (Exception e) {
                log_msg(String.format("[%s] %s", (req != null) ? req.uuid : '?', e.message))
            }
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

        defaultGen = gen
        defaultIter = iter
        genMap.put(gen + iter, new ArrayList<ItemsetGenerator>() {{ add((ItemsetGenerator) gener) }})
    }

    public Response doRequest(Request req) {
        if (req.type == ReqType.GET) {
            def start = System.currentTimeMillis()
            def data = generator.getFrequentItemsets(req.supPct, req.maxPct)
            return new Response(Result.SUCCESS, Reason.GET, data, System.currentTimeMillis() - start)
        }
        else if (req.type == ReqType.SET) {
            SetRequest sq = (SetRequest) req
            generator.release()
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
        else if (strs.length == 3 && strs[0].equalsIgnoreCase("get")) {
            for (int i = 2; i < strs.length; i++)
                arr.add(strs[i])

            return new Request(strs[0], strs[1], strs[2], arr)
        }
        else throw new UnsupportedOperationException()
    }

    public synchronized Response getGenerator(String gen, String iter) {
        // Try to load the generator from the map; if it doesn't yet exist we
        // create it
        if ((generator = getUnused(gen + iter)) == null) {
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
            def list = genMap.get(gen + iter)
            if (list == null) list = new ArrayList<ItemsetGenerator>()
            list.add(generator)
            log("Adding new itemset generator: " + gen + ", " + iter)
            genMap.put(gen + iter, list)
        }

        return new Response(Result.SUCCESS, Reason.SET, null, 0)
    }

    public synchronized ItemsetGenerator getUnused(String key) {
        def list = genMap.get(key)

        if (list != null) {
            for (def item in list) {
                if (item.lock())
                    return item
            }
        }

        return null
    }

    // Return the unique ID assigned to this client
    public String toString() {
        uuid.toString()
    }

    public void log(String message) {
        log_msg(String.format("[%s] %s", uuid, message))
    }

    public static void log_msg(String message) {
        System.err.printf(LOG_FORMAT, DATE_FORMAT.format(new Date()), "debug", message)
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