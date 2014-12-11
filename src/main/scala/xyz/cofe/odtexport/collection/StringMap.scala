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

package xyz.cofe.odtexport.collection

/**
 * Карта String / String
 */
object StringMap
{
	import java.io.File;
	import java.util.Properties;
	import java.io.FileInputStream;
	import java.io.FileOutputStream;
	import xyz.cofe.odtexport.Common.using;

	/**
	 * Чтение набора свойств.
	 * @param props Набор свойств
	 * @return Карта строк
	 */
	def propertiesToStringMap( props:Properties ):Map[String,String] = {
		import collection.JavaConversions._;
		
		if( props==null )throw new IllegalArgumentException("props==null");
		
		var res = new scala.collection.immutable.HashMap[String,String]();
		props.keySet().foreach( (k)=>{
			if( k!=null && k.isInstanceOf[String] ){
				val o = props.get(k);
				if( o!=null && o.isInstanceOf[String] ){
					val key = k.asInstanceOf[String];
					val obj = o.asInstanceOf[String];
					res = res + ( (key, obj) ) ;
				}
			}
		});
		
		/*for( val k <- props.keySet() ){
			if( k!=null && k.isInstanceOf[String] ){
				val o = props.get(k);
				if( o!=null && o.isInstanceOf[String] ){
					val key = k.asInstanceOf[String];
					val obj = o.asInstanceOf[String];
					res = res + ( (key, obj) ) ;
				}
			}
		}*/
		res
	}

	/**
	 * Конвертирование карты строк в набор свойств
	 * @param Карта строк
	 * @return Набор свойств
	 */
	def stringMapToProperties( map:Map[String,String] ):Properties = {
		if( map==null )throw new IllegalArgumentException("map==null");
		val props = new Properties();
		for( (k,v) <- map ){
			if( k!=null )props.put( k, v );
		}
		props
	}

	/**
	 * Объединяет несколько карт в одну
	 */
	def join( maps:Map[String,String]* ):Map[String,String] = {
		var res = Map[String,String]();
		for( m <- maps ){
			res = res ++ m;
		}
		res
	}

	/**
	 * Чтение файла настроек.
	 * В зависимости от расширения (.xml/.properties) соответ. обрабразом и происзодит чтение.
	 * @param url Файл
	 * @return Карта настроек
	 */
	def readStringMap( url : java.net.URL ) : Map[String,String] =
	{
		import xyz.cofe.odtexport.InputStream._;

		if( url == null ) throw new IllegalArgumentException("url==null");
		val props = new Properties();
		if( url.getFile().toLowerCase().endsWith(".xml") ){
			using( url.openStream() ){
				case( input ) => props.loadFromXML(input);
			}
		}else{
			using( url.openStream() ){
				case( input ) =>
					val txt = input.readText("UTF-8");
					val reader = new java.io.StringReader(txt);
					props.load(reader);
			}
		}
		propertiesToStringMap(props)
	}

	/**
	 * Чтение файла настроек.
	 * В зависимости от расширения (.xml/.properties) соответ. обрабразом и происзодит чтение.
	 * @param file Файл
	 * @return Карта настроек
	 */
	def readStringMap(file : File) : Map[String,String] = {
		import xyz.cofe.odtexport.InputStream._;

		if( file == null ) throw new IllegalArgumentException("file==null");
		val props = new Properties();
		val fin = new FileInputStream(file);
		if( file.getName().toLowerCase().endsWith(".xml") ){
			props.loadFromXML(fin);
		}else{
			val txt = fin.readText("UTF-8");
			val reader = new java.io.StringReader(txt);
			props.load(reader);
		}
		fin.close();
		propertiesToStringMap(props)
	}

	/**
	 * Пишет файл настроек.
	 * В зависимости от расширения (.xml/.properties) соответ. обрабразом и происзодит запись.
	 * @param file Файл
	 * @param map Карта
	 */
	def writeStringMap(file:File, map:Map[String,String]):Unit = {
		if(file==null)throw new IllegalArgumentException("file==null");
		if(map==null)throw new IllegalArgumentException("map==null");

		val props = stringMapToProperties(map);
		if( file.getName().toLowerCase().endsWith(".xml") ){
			val fout = new FileOutputStream(file);
			props.storeToXML(fout,"none");
			fout.close();
		}else{
			import xyz.cofe.odtexport.File._;
			val writer = file.openWriter("UTF-8",false);
			props.store(writer,"none");
			writer.close();
		}

	}
}