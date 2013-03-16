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
class ItemsetRequester<Item> {
    static final int PORT = 8100
    static final String BAD_GENERATOR = "Itemset generator %s was not found"
    static final String BAD_ITERATOR = "Iterator %s was not found"
    static final String USAGE = "java ItemsetRequester <generator-name> <iterator>"

    private int port = PORT
    private ItemsetGenerator generator

    private static enum ReqType { GET, UPDATE }
    private static enum Result { SUCCESS, FAILURE }

    private class Request<Item> {
        private final ReqType type
        private final double supPct
        private List<Basket<Item>> baskets

        public Request(String type, String supPct, List<String> data)
        throws NumberFormatException {
            baskets = new ArrayList<Basket<Item>>()
            this.supPct = Double.parseDouble(supPct)
            this.type = ReqType.valueOf(ReqType, type.toUpperCase())

            for (String gn : data)
                baskets.add(new Basket<Item>(gn))
        }
    }

    private class Response<Item> {
        public final Result res
        public final String message
        public Map<List<Item>, Integer> data

        public Response(Result res, String message, def data) {
            this.res = res
            this.message = message
            this.data = data
        }

        @Override
        public String toString() { return new Gson().toJson(this) }
    }

    public ItemsetRequester(ItemsetGenerator gen) {
        generator = gen
    }

    public boolean start() throws IOException {
        boolean done = false
        def server = new ServerSocket(port)
        System.err.printf("Server started on port %d; waiting for clients.\n", port)

        while (!done) {
            String input = ""
            Response output
            def client = server.accept()
            def reader = new Scanner(client.getInputStream())
            def writer = new PrintWriter(client.getOutputStream())

            if ((input = reader.nextLine()) != null) {
                System.err.println("Client connected.")

                try {
                    output = doRequest(parseRequest(input))
                }
                catch (NumberFormatException e) {
                    output = new Response(Result.FAILURE,
                      "The minSup must be a double between 0 and 1.", null)
                }
                catch (UnsupportedOperationException e) {
                    output = new Response(Result.FAILURE,
                      "Request not valid: must contain at least 2 words.", null)
                }

                writer.println(output)
            }

            generator.reset()
            writer.close()
            reader.close()
            client.close()
        }

        server.close()
        return true
    }

    public Response doRequest(Request req) {
        if (req.type == ReqType.GET) {
            def data = generator.getFrequentItemsets(req.supPct)
            return new Response(Result.SUCCESS, "", data)
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

        if (strs.length >= 2) {
            for (int i = 2; i < strs.length; i++)
                arr.add(strs[i])

            return new Request(strs[0], strs[1], arr)
        }
        else throw new UnsupportedOperationException()
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(USAGE)
            System.exit(-1)
        }

        String gen = args[0]
        String data = args[1]
        def constr = Class.forName(gen).getConstructor([BasketIterator] as Class[])
        def bask = Class.forName(data).newInstance()
        def gener = constr.newInstance(bask)

        if (!(gener instanceof ItemsetGenerator)) {
            System.err.printf(BAD_GENERATOR + "\n", gen)
            System.exit(-1)
        }
        if (!(bask instanceof BasketIterator)) {
            System.err.printf(BAD_ITERATOR + "\n", data)
            System.exit(-1)
        }

        def requester = new ItemsetRequester((ItemsetGenerator) gener)

        try {
            requester.start()
        }
        catch (IOException e) {
            e.printStackTrace()
            System.exit(-1)
        }
    }
}