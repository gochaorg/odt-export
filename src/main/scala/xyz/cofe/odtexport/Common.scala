/* 
 * The MIT License
 *
 * Copyright 2014 Kamnev Georgiy (nt.gocha@gmail.com).
 *
 * Данная лицензия разрешает, безвозмездно, лицам, получившим копию данного программного 
 * обеспечения и сопутствующей документации (в дальнейшем именуемыми "Программное Обеспечение"), 
 * использовать Программное Обеспечение без ограничений, включая неограниченное право на 
 * использование, копирование, изменение, объединение, публикацию, распространение, сублицензирование 
 * и/или продажу копий Программного Обеспечения, также как и лицам, которым предоставляется 
 * данное Программное Обеспечение, при соблюдении следующих условий:
 *
 * Вышеупомянутый копирайт и данные условия должны быть включены во все копии 
 * или значимые части данного Программного Обеспечения.
 *
 * ДАННОЕ ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ ЛЮБОГО ВИДА ГАРАНТИЙ, 
 * ЯВНО ВЫРАЖЕННЫХ ИЛИ ПОДРАЗУМЕВАЕМЫХ, ВКЛЮЧАЯ, НО НЕ ОГРАНИЧИВАЯСЬ ГАРАНТИЯМИ ТОВАРНОЙ ПРИГОДНОСТИ, 
 * СООТВЕТСТВИЯ ПО ЕГО КОНКРЕТНОМУ НАЗНАЧЕНИЮ И НЕНАРУШЕНИЯ ПРАВ. НИ В КАКОМ СЛУЧАЕ АВТОРЫ 
 * ИЛИ ПРАВООБЛАДАТЕЛИ НЕ НЕСУТ ОТВЕТСТВЕННОСТИ ПО ИСКАМ О ВОЗМЕЩЕНИИ УЩЕРБА, УБЫТКОВ 
 * ИЛИ ДРУГИХ ТРЕБОВАНИЙ ПО ДЕЙСТВУЮЩИМ КОНТРАКТАМ, ДЕЛИКТАМ ИЛИ ИНОМУ, ВОЗНИКШИМ ИЗ, ИМЕЮЩИМ 
 * ПРИЧИНОЙ ИЛИ СВЯЗАННЫМ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ 
 * ИЛИ ИНЫМИ ДЕЙСТВИЯМИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ.
 */

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