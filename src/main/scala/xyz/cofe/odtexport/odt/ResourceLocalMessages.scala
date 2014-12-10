package xyz.cofe.odtexport.odt


/**
 * Поддержка локализации.
 */
class ResourceLocalMessages(val resourceName:String)
{
	import xyz.cofe.odtexport.collection.StringMap._;

	lazy val messages : Map[String,String] = {
		val lang = java.util.Locale.getDefault.getLanguage;
		val lrc = if(resourceName.contains("."))
				resourceName.substring(0,resourceName.lastIndexOf(".")) + "_" + lang +
				resourceName.substring(resourceName.lastIndexOf("."));
			else
				resourceName + "_" + lang;

		val urlLang = this.getClass.getResource( lrc );
		val urlDef = this.getClass.getResource( resourceName );

		var res = Map[String,String]();
		if( urlDef!=null )res = join(res,readStringMap(urlDef));
		if( urlLang!=null )res = join(res,readStringMap(urlLang));
		res
	}
}