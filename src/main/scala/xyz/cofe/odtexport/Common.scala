package xyz.cofe.odtexport

object Common {
	/**
	 *  Конструкция автоматически закрывает ресурсы:<br/>
	 * <code>
	 * using( new FileOutputStream(...), new Connection(...) )  { case (file,db) => ... }
	 * </code>
	 */
	def using[Arguments,Result]( a:Arguments )( f: Arguments => Result ):Result = {
		import java.sql._;

		def closeIO(o:java.io.Closeable) = {
			o.close();
		}
		def closeConnection(o:Connection) = {
			o.close();
		}
		def closeStatement(o:Statement) = {
			o.close();
		}
		def closeResultSet(o:ResultSet) = {
			o.close();
		}
		def closeProduct(o:Product):Unit = {
			for( i <- 0 to o.productArity-1 ){
				closeAny( o.productElement(i) );
			}
		}
		def closeAny(o:Any) = {
			if( o!=null ){
				o match {
					case x:java.io.Closeable => closeIO(x);
					case x:Product => closeProduct(x);
					case x:Connection => closeConnection(x);
					case x:ResultSet => closeResultSet(x);
					case x:Statement => closeStatement(x);
					case _ => ;
				}
			}
		}
		try{
			val res = f(a);
			closeAny(res);
			res
		}
		finally{
			closeAny(a);
		}
	}
}