import me.therin.mining.itemsets.fpgrowth.FPTree
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException
import java.sql.Statement
import java.sql.ResultSet
import me.therin.brovine.Transfac;

/**
 * The second and third examples are from the book (Data Mining).
 */
def arrs = [
    //[[1,2], [3,4], [5,6]],

    //[['b','a'], ['d','c','b'], ['e','d','c','a']],

    [['b','a'], ['d','c','b'], ['e','d','c','a'], ['e', 'd', 'a'], ['c', 'b', 'a'], ['d', 'c', 'b', 'a'], ['a'],
     ['c', 'b', 'a'], ['d', 'b', 'a'], ['e', 'c', 'b']],
]

def ans = [
    "0:-1,1;1:2,1;2:1,1;1:4,1;2:3,1;1:6,1;2:5,1;",
    "0:-1,1;1:a,2;2:b,1[b,1];2:c,1;3:d,1;4:e,1;1:b,1;2:c,1[c,1];3:d,1[d,1];",
    ""
]

def list = new ArrayList<FPTree>()

/*arrs.eachWithIndex { it, i ->
    def tree = new FPTree(it, 2)
    list.add(tree)
    def othertree = tree.getPrefixTree('e')
    System.out.println(tree.toString())
    System.out.println(tree.getFrequentItemsets(['e','d','c','b','a']))
}*/

/**
 * Connecting to MySQL thru Connector/J
 */

try {
    // The newInstance() call is a work around for some
    // broken Java implementations

    Class.forName("com.mysql.jdbc.Driver").newInstance();
} catch (Exception ex) {
    // handle the error
}

ResultSet rs
Statement stmt
Connection conn

try {
    conn =
        DriverManager.getConnection("jdbc:mysql://localhost/brovine?" +
                "user=team_brovine&password=v9G4uJn7Rta9vQvN");

    stmt = conn.createStatement()
    rs = stmt.executeQuery("SELECT * FROM apriori_staging")


}
catch (SQLException ex) {
    // handle any errors
    System.out.println("SQLException: " + ex.getMessage());
    System.out.println("SQLState: " + ex.getSQLState());
    System.out.println("VendorError: " + ex.getErrorCode());
}
finally {
    // it is a good idea to release
    // resources in a finally{} block
    // in reverse-order of their creation
    // if they are no-longer needed

    if (rs != null) {
        try {
            rs.close();
        } catch (SQLException sqlEx) { } // ignore

        rs = null;
    }

    if (stmt != null) {
        try {
            stmt.close();
        } catch (SQLException sqlEx) { } // ignore

        stmt = null;
    }
}

final String UNIQUE_ITEMS =
    """
SELECT DISTINCT(tf_cart) as matchid, transfac
FROM apriori_staging
INNER JOIN factor_matches ON (tf_cart = matchid)
ORDER BY matchid
"""
try {
    Statement state
    List<Transfac> tfs = new ArrayList<Transfac>()

    state = conn.createStatement()
    rs = state.executeQuery(UNIQUE_ITEMS)

    while (rs.next()) {
        tfs.add(new Transfac(rs.getInt(1), rs.getString(2)))
    }

    tfs.each {
        System.out.println(it.id + ": " + it.name)
    }
}
catch (SQLException e) { e.printStackTrace() }
