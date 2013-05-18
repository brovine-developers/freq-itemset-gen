package me.therin.brovine

import me.therin.brovine.Settings
import me.therin.mining.itemsets.data.BasketIterator
import me.therin.mining.itemsets.data.Basket
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * TransfacBaskets.java
 *
 * @author tcirwin
 * @date 03 09, 2013
 */
class TransfacBaskets implements BasketIterator<Transfac> {
    private Connection conn
    private static final String UNIQUE_ITEMS =
"""
SELECT DISTINCT(tf_cart) as matchid, transfac, g.cnt
FROM apriori_staging
INNER JOIN factor_matches ON (tf_cart = matchid)
INNER JOIN (select tf, cnt from (
select tf_cart as tf, count(*) as cnt
from apriori_staging
group by tf_cart
) f) g ON (tf = tf_cart)
ORDER BY matchid;
"""

    private static final String FILL_BASKETS =
"""
SELECT geneid, tf_cart, transfac
FROM (select tf_cart
from (
select tf_cart, count(*) as cnt
from apriori_staging
group by tf_cart
) f) g
INNER JOIN apriori_staging USING (tf_cart)
INNER JOIN factor_matches ON (tf_cart = matchid)
ORDER BY geneid, tf_cart
"""

    private static final String NUM_BASKETS =
"""
SELECT COUNT(DISTINCT geneid)
FROM apriori_staging
"""

    private List<Basket<Transfac>> baskets
    private Iterator<Basket<Transfac>> itr

    public TransfacBaskets() {
        initConnection()
        fillBaskets()
    }

    private void initConnection() {
        try {
            def script = new GroovyScriptEngine( './lib' ).with {
               loadScriptByName( 'passwd.groovy' )
            }

            Class.forName(script.driver).newInstance()
            conn = DriverManager.getConnection(script.url, script.user, script.pass)
        }
        catch (SQLException ex) {
            // handle any errors
            System.err.println("SQLException: " + ex.getMessage())
            System.err.println("SQLState: " + ex.getSQLState())
            System.err.println("VendorError: " + ex.getErrorCode())
            ex.printStackTrace()
        }
        catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    @Override
    List<Transfac> getUniqueItems() {
        try {
            Statement state
            ResultSet rs
            List<Transfac> tfs = new ArrayList<Transfac>()

            if (conn == null || conn.isClosed())
                initConnection()

            state = conn.createStatement()
            rs = state.executeQuery(UNIQUE_ITEMS)

            while (rs.next()) {
                tfs.add(new Transfac(rs.getInt(1), rs.getString(2)))
            }

            return tfs
        }
        catch (SQLException e) { e.printStackTrace() }
    }

    @Override
    //TODO: implement includeOnly
    BasketIterator<Transfac> includeOnly(List<Basket<Transfac>> baskets) {
        return null
    }

    @Override
    //TODO: implement update
    boolean update() {
        return false
    }

    @Override
    boolean reset() {
        if (fillBaskets()) {
            itr = baskets.iterator()
            return true
        }

        return false
    }

    @Override
    int size() {
        try {
            Statement state
            ResultSet rs

            if (conn.isClosed())
                initConnection()

            state = conn.createStatement()
            rs = state.executeQuery(NUM_BASKETS)

            rs.next() ? rs.getInt(1) : 0
        }
        catch (SQLException e) { e.printStackTrace() }
    }

    @Override
    boolean hasNext() { (fillBaskets()) ? itr.hasNext() : false }

    @Override
    Basket<Transfac> next() { (fillBaskets()) ? itr.next() : null }

    private boolean fillBaskets() {
        if (itr == null) {
            try {
                int curGene = -1
                Statement state
                ResultSet rs
                def basket = null
                baskets = new ArrayList<Basket<Transfac>>()

                if (conn == null || conn.isClosed())
                    initConnection()

                state = conn.createStatement()
                rs = state.executeQuery(FILL_BASKETS)

                while (rs.next()) {
                    if (basket == null || curGene != rs.getInt(1)) {
                        curGene = rs.getInt(1)
                        if (basket != null) baskets.add(basket)
                        basket = new Basket(curGene.toString())
                    }

                    basket.add(new Transfac(rs.getInt(2), rs.getString(3)))
                }

                itr = baskets.iterator()

                return true
            }
            catch (SQLException e) {
                e.printStackTrace()
                return false
            }
        }
        else return true
    }

    @Override
    void remove() {
        throw new UnsupportedOperationException()
    }
}
