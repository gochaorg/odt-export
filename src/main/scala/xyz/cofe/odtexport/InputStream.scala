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

import java.io.{
	InputStream => IStream,
	InputStreamReader=>ISReaded,
	BufferedReader =>BReader,
	OutputStream => OStream
};
import java.nio.charset.Charset;

object InputStream {
	class InputStreamExtension (val is:IStream) {
		/**
		 * Читает текст из потока, до конца
		 * @param cs - Кодировка текста
		 * @return Декодированный текст
		 */
		def readText(cs:Charset):String = {
			//val res = new StringBuilder();
//			val isr = new ISReaded( is, cs );
//			val br = new BReader( isr );
			var resbuff : Array[Byte] = Array[Byte](0);
			val buff = new Array[Byte](1024*4);
			var stop = false;
			while( !stop ){
				val readed = is.read(buff);
				if( readed<0 ){
					stop = true;
				}else{
					if( readed>0 ){
						resbuff = java.util.Arrays.copyOf(resbuff, resbuff.length + readed);
						System.arraycopy(buff, 0, resbuff, resbuff.length - readed, readed);
					}
				}
			}
			new java.lang.String(resbuff,cs)
		}

		/**
		 * Читает текст из потока, до конца
		 * @param cs - Кодировка текста
		 * @return Декодированный текст
		 */
		def readText( cs:String ):String = readText( Charset.forName(cs) );

		/**
		 * Читает текст из потока, до конца. Исползуется кодировка по умолчанию.
		 * @return Декодированный текст
		 */
		def readText():String = readText( Charset.defaultCharset );
	}

	implicit def extendInputStream( is:IStream ):InputStreamExtension = new InputStreamExtension( is );
}