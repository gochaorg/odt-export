package xyz.cofe.odtexport.odt

import xyz.cofe.odtexport.xtest.XMLPathVisitor;
import xyz.cofe.odtexport.xtest.NodePatternsVisitor;
import xyz.cofe.odtexport.xtest.NodePattern;
import xyz.cofe.odtexport.odt.style.Styles;

/**
 * Создает HTML документ из ODT 
 */
class ODTHtmlBuilder
	extends XMLPathVisitor
	with NodePatternsVisitor
	with ODTXMLPatterns
{
	/**
	 * Здесь будут собранные все шаблоные (условия XTest и правила обработки)
	 * Для обработки документа ODT (XML)
	 */
	def patterns = _patterns;
	private lazy val _patterns : List[NodePattern] = {
		outputHtmlHeaderFooter :::
		outputImage :::
		outputCode :::
		outputHeader :::
		outputLists :::
		outputBookmarks :::
		outputNotes :::
		outputIncuts :::
		outputTable :::
		outputLinks :::
		outputText
	};

	/**
	 * Здесь содержится html документ
	 */
	val out : StackOutput = new StackOutput;

	/**
	 * Здесь будут содержаться стили для текущего обрабатываемого документа
	 */
	private var _styles : Styles = null;
	def styles : Styles = _styles;

	import org.w3c.dom.{Node => XMLNode};

	// Подлючаем функции для описания условий XTest
	// Как то And оно же оператор "&", Or оно же "|" и других
	import xyz.cofe.odtexport.xtest._;
	
	// Подключаем возможность упрощенно конвертировать XTest в NodePattern
	import xyz.cofe.odtexport.xtest.NodePattern._;

	/**
	 * Возвращает текст XML атрибута
	 * @param attribute Атрибут
	 * @param node XML Узел
	 * @param defValue Значение по умолчанию
	 */
	protected def attr( attribute:String, node:XMLNode = path.top, defValue:String="" ) = {
		val attr = node.getAttributes().getNamedItem(attribute);
		if( attr!=null )
			attr.getNodeValue()
		else
			defValue
	}

	/**
	 * Отображение заголовка html (включая doctype, html, head, meta, title, body ...) и
	 * завершения html (body, html)
	 */
	lazy val outputHtmlHeaderFooter = List[NodePattern](
		FalseXTest()
			begin {
				out << "<!DOCTYPE html>";

				out << "<html>";
				out << "<head>";
				out << "<meta http-equiv=\"Content-type\" content=\"text/html;charset=UTF-8\" />";
				out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />";
				out << "<title>Exported ODT</title>";
				out << "</head>";
				out << "<body>";
			}
			end {
				out << "</body>";
				out << "</html>";
			}
	);

	/**
	 * Отображение списоков
	 */
	lazy val outputLists : List[NodePattern] = {
		List[NodePattern](
			list.orderedList >> list.list
				enter { out << "<ol>"; }
				exit { out << "</ol>"; },

			list.orderedList
				enter { out << "<ol>"; }
				exit { out << "</ol>"; },

			list.list
				enter { out << "<ul>"; }
				exit { out << "</ul>"; },

			list.item
				enter { out << "<li>"; }
				exit { out << "</li>"; }
		);
	};

	/**
	 * Кодирует текст (#text, Просто параграф) в HTML
	 */
	lazy val outputText : List[NodePattern] = {
		/**
		 * Кодирует текст в HTML
		 */
		def encodeHtml( txt:String ):String = {
				txt.replace("&","&amp;").replace( "<","&lt;" ).replace( ">","&gt;" ).replace( "\"","&quot;" )
			}

		/**
		 * Возвращает текст узла
		 * @param n Узел
		 */
		def textOf( n:XMLNode ) = n.getTextContent();

		/**
		 * Возвращает html узла
		 * @param n Узел
		 */
		def htmlOf( n:XMLNode ) = encodeHtml( textOf( n ) );

		List[NodePattern](
			paragraph enter {out < "<p>"} exit {out << "</p>"}
			,lineBreak enter {out << "<br />"; }
			,text enter { out < htmlOf( path.top ); }
		);
	}
	
	/**
	 * Кодирует заголовки в теги H1 .. H7
	 */
	lazy val outputHeader = List[NodePattern](
		textHeader +@ "text:outline-level" == "1" enter {out < "<h1>"} exit {out << "</h1>"}
		,textHeader +@ "text:outline-level" == "2" enter {out < "<h2>"} exit {out << "</h2>"}
		,textHeader +@ "text:outline-level" == "3" enter {out < "<h3>"} exit {out << "</h3>"}
		,textHeader +@ "text:outline-level" == "4" enter {out < "<h4>"} exit {out << "</h4>"}
		,textHeader +@ "text:outline-level" == "5" enter {out < "<h5>"} exit {out << "</h5>"}
		,textHeader +@ "text:outline-level" == "6" enter {out < "<h6>"} exit {out << "</h6>"}
		,textHeader +@ "text:outline-level" == "7" enter {out < "<h7>"} exit {out << "</h7>"}
		);

	/**
	 * Кодирует текст указанный как исходный код:<br/>
	 * Параграфы со стилем: Code; табуляция;<br/>
	 * Параграфы со стилем: Code; / Символы со стилем:
	 * Code Key Word | Code Comment | Code Input.
	 */
	lazy val outputCode : List[NodePattern] = {
		def Code = paragraph & style.name("Code");

		import collection.mutable.MutableList;

		var res = new MutableList[NodePattern]();

		// Одиночная строка кода
		res += !Code - Code + !Code enter { out < "<code>" } exit { out << "</code><br/>" };

		// Насало кода
		res += !Code - Code + Code enter { out < "<code>" } exit { out << "<br/>" };

		// Строка кода в середине
		res += Code - Code + Code  exit { out << "<br/>" };

		// Конец кода
		res += Code - Code + !Code exit { out << "</code ><br />" };

		// Табуляция в коде
		res += Code >> tab enter {} exit { out < "&nbsp;&nbsp;&nbsp;&nbsp;" };

		// Ключевое слово
		res += Code >> TagName("text:span") & style.name("Code Key Word") enter {
			out < "<span class=\"codeKeyWord\">"; } exit {
			out < "</span>" };

		// Комментарий
		res += Code >> TagName("text:span") & style.name("Code Comment") enter {
			out < "<span class=\"codeComment\">"; } exit {
			out < "</span>" };

		// Ввод пользователя
		res += Code >> TagName("text:span") & style.name("Code Input") enter {
			out < "<span class=\"codeInput\">"; } exit {
			out < "</span>" };

		res.toList
	}

	/**
	 * Отображение закладок
	 */
	lazy val outputBookmarks : List[NodePattern] = {
		List[NodePattern](
			bookmark.start
				enter { out < "<a name=\""+attr("text:name")+"\" class=\"bookmark\">"; },

			bookmark.end
				exit { out < "</a>"; },

			bookmark.ref
				enter { out < "<a href=\"#"+attr("text:ref-name")+"\">"; }
				exit { out < "</a>"; },

			textSequence.sequence
				enter { out < "<a name=\""+attr("text:ref-name")+"\" class=\"bookmark\">"; }
				exit { out < "</a>"; },

			textSequence.ref
				enter { out < "<a href=\"#"+attr("text:ref-name")+"\">"; }
				exit { out < "</a>"; },

			textMark.start
				enter { out < "<a name=\""+attr("text:name")+"\" class=\"bookmark\">"; },

			textMark.end
				exit { out < "</a>"; },

			textMark.ref
				enter { out < "<a href=\"#"+attr("text:ref-name")+"\">"; }
				exit { out < "</a>"; }
		);
	}

	/**
	 * Отображение картинок
	 */
	lazy val outputImage = {
		val imgSizePttrn = java.util.regex.Pattern.compile(
			"(?is)^([\\-+]?\\s*\\d+)(\\.\\d*)?(cm|im|mm|pt|pc|px)?$"
			);

		def imgSize( size:String ):String = {
			if( size==null )return size;
			val m = imgSizePttrn.matcher(size);
			if( m.matches() ){
				val intDgts = m.group(1);
				val fltDgts = m.group(2);
				val sys = m.group(3);
				if( sys==null )return size;
				if( sys.equalsIgnoreCase("px") )return size;

				val num : Double = {
					(intDgts + (if (fltDgts!=null) fltDgts else "")).toDouble;
				};

				val nPx  = sys.toLowerCase() match {
					case "cm" => (((num * (1.0/2.54)) * 72.0) / 0.75).toInt;
					case "im" => ((num * 72.0) / 0.75).toInt;
					case "mm" => ((((num / 10.0) * (1.0/2.54)) * 72.0) / 0.75).toInt;
					case "pt" => (num / 0.75).toInt;
					case "pc" => ((num / 12.0) / 0.75).toInt;
					case _ => num.toInt;
				}

				return (""+nPx)+"px";
			}
			size
		}

		def getImageHref( node : XMLNode ) : String = {
			var href : String = null;
			val o2 = new NodePatterns(
				image.image enterNode(
						(n) => {href = attr("xlink:href",n);}
						)
				);

			XMLVisitor.go( node, o2 );
			return href;
		}

		def getImageHrefAndSize():(String,String,String) = {
			var href : String = getImageHref(path.top);

			val svgWidth = attr("svg:width");
			val svgHeight = attr("svg:height");

			val pxWidth = imgSize(svgWidth);
			val pxHeight = imgSize(svgHeight);

			(href,pxWidth,pxHeight)
		}

		List[NodePattern](
			// Картинка вставлена как символ
			TagName( "draw:frame" ) & anchor.asChar >
			TagName( "draw:text-box" ) >
			TagName( "text:p" ) >
			image.frame <
			image.image
				skip {
					val (href,width,height) = getImageHrefAndSize();
					out <
						"<img border=\"0\" " +
						"src=\""+href+"\" " +
						"width=\""+width+"\" " +
						"height=\""+height+"\" />";
				},

			// Картинка с размерами, без обтекания слева
			image.frame & anchor.toParagraph & align.horz.left & style.noWrap <
			image.image
				skip {
					val (href,width,height) = getImageHrefAndSize();
					out <
						"<img border=\"0\" " +
						"class=\"nowrap left\" "+
						"src=\""+href+"\" " +
						"width=\""+width+"\" " +
						"height=\""+height+"\" />";
				},

			// Картинка с размерами, без обтекания справа
			image.frame & anchor.toParagraph & align.horz.right & style.noWrap <
			image.image
				skip {
					val (href,width,height) = getImageHrefAndSize();
					out <
						"<img border=\"0\" " +
						"class=\"nowrap right\" "+
						"src=\""+href+"\" " +
						"width=\""+width+"\" " +
						"height=\""+height+"\" />";
				},

			// Картинка с размерами, без обтекания по центру
			image.frame & anchor.toParagraph & align.horz.center & style.noWrap <
			image.image
				skip {
					val (href,width,height) = getImageHrefAndSize();
					out <
						"<img border=\"0\" " +
						"class=\"nowrap center\" "+
						"src=\""+href+"\" " +
						"width=\""+width+"\" " +
						"height=\""+height+"\" />";
				},

			// Картинка с размерами, с обтеканим справа
			image.frame & anchor.toParagraph & align.horz.right <
			image.image
				skip {
					val (href,width,height) = getImageHrefAndSize();
					out <
						"<img border=\"0\" " +
						"class=\"right\" "+
						"src=\""+href+"\" " +
						"width=\""+width+"\" " +
						"height=\""+height+"\" />";
				},

			// Картинка с размерами, с обтеканим слева
			image.frame & anchor.toParagraph & align.horz.left <
			image.image
				skip {
					val (href,width,height) = getImageHrefAndSize();
					out <
						"<img border=\"0\" " +
						"class=\"left\" "+
						"src=\""+href+"\" " +
						"width=\""+width+"\" " +
						"height=\""+height+"\" />";
				},

			// Картинка с размерами как символ, выравнивание по центру
			image.frame & anchor.asChar & align.vert.center <
			image.image
				skip {
					val (href,width,height) = getImageHrefAndSize();
					out <
						"<img border=\"0\" " +
						"class=\"vert-middle\" "+
						"src=\""+href+"\" " +
						"width=\""+width+"\" " +
						"height=\""+height+"\" />";
				},

			// Картинка с размерами как символ, выравнивание по низу
			image.frame & anchor.asChar & align.vert.bottom <
			image.image
				skip {
					val (href,width,height) = getImageHrefAndSize();
					out <
						"<img border=\"0\" " +
						"class=\"vert-bottom\" "+
						"src=\""+href+"\" " +
						"width=\""+width+"\" " +
						"height=\""+height+"\" />";
				},

			// Картинка с размерами
			image.frame <
			image.image
				skip {
					val (href,width,height) = getImageHrefAndSize();
					out <
						"<img border=\"0\" " +
						"src=\""+href+"\" " +
						"width=\""+width+"\" " +
						"height=\""+height+"\" />";
				},

			// Любая картинка
			image.image
				enter { out << "<img src=\""+attr("xlink:href")+"\" />"; }
		)
	};

	/**
	 * Отображение таблиц
	 */
	lazy val outputTable : List[NodePattern] = {
		List[NodePattern](
		// Начало/Конец таблицы
		table.table
			enter { out << "<table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">" }
			exit { out << "</table>" }

		// Заголовок таблицы
		,table.headerRow
			enter {out << "<tr class=\"tableHeader\">"}
			exit {out << "</tr>"}

		// Строка таблицы
		,table.row
			enter {out << "<tr>"}
			exit {out << "</tr>"}

		// Ячейка со стилем Table Heading - т.е. Заголовок таблицы.
		,table.cellWithRowColSpan < ( paragraph & style.name("Table Heading") )
			enter {out < "<td rowspan=\""+attr("table:number-rows-spanned")+"\" colspan=\""+attr("table:number-columns-spanned")+" class=\"tableHeader\">"}
			exit {out < "</td>"}

		// Ячейка со стилем Table Heading - т.е. Заголовок таблицы.
		,table.cellWithRowSpan < ( paragraph & style.name("Table Heading") )
			enter {out < "<td rowspan=\""+attr("table:number-rows-spanned")+"\" class=\"tableHeader\">"}
			exit {out < "</td>"}

		// Ячейка со стилем Table Heading - т.е. Заголовок таблицы.
		,table.cellWithColSpan < ( paragraph & style.name("Table Heading") )
			enter {out < "<td colspan=\""+attr("table:number-columns-spanned")+"\" class=\"tableHeader\">"}
			exit {out < "</td>"}

		// Ячейка со стилем Table Heading - т.е. Заголовок таблицы.
		,table.cell < ( paragraph & style.name("Table Heading") )
			enter {out < "<td class=\"tableHeader\">"}
			exit {out < "</td>"}

		// Ячейка таблицы с пропуском строк и колонок
		,table.cellWithRowColSpan
			enter {out < "<td rowspan=\""+attr("table:number-rows-spanned")+"\" colspan=\""+attr("table:number-columns-spanned")+"\">"}
			exit {out < "</td>"}

		// Ячейка таблицы с пропуском строк
		,table.cellWithRowSpan
			enter {out < "<td rowspan=\""+attr("table:number-rows-spanned")+"\">"}
			exit {out < "</td>"}

		// Ячейка таблицы с пропуском колонок
		,table.cellWithColSpan
			enter {out < "<td colspan=\""+attr("table:number-columns-spanned")+"\">"}
			exit {out < "</td>"}

		// Ячейка таблицы
		,table.cell
			enter {out < "<td>"}
			exit {out < "</td>"}
		);
	}

	/**
	 * Отображение врезки
	 */
	lazy val outputIncuts : List[NodePattern] = {
		List[NodePattern](
	        // Определяет врезку
	            incut & align.horz.right & anchor.toParagraph
	                enter { out << "<div class=\"incut right\">"; }
	                exit { out << "</div>"; }

	        // Врезка слева
	        ,   incut & align.horz.left & anchor.toParagraph
					enter { out << "<div class=\"incut left\">"; }
	                exit { out << "</div>"; }

	        // Любая врезка
	        ,   incut & anchor.toParagraph
					enter { out << "<div class=\"incut center\">"; }
	                exit { out << "</div>"; }
	    );
	}

	private var notes = List[List[String]]();

	/**
	 * Отображение сносок
	 */
	lazy val outputNotes = {
		List[NodePattern](
			TagName("text:note")
				enter { out.push(); }
				exitNode( (n)=> {
					val num = 1 + notes.length;
					val noteCitation = "["+num+"]";

					notes = ( List( noteCitation, out.pop() ) ) :: notes;
					out < "<a class=\"note\" href=\"#note_"+notes.length.toString+"\">";
					out < noteCitation;
					out < "</a>";
				} )
				end {
					if( notes.length > 0 ){
						out << "<p class=\"notesTitle\">Сноски:</p>";
						out << "<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\" class=\"notes\">";
						var idx = 0;
						for( note <- notes.reverse ){
							val noteCitation = note(0);
							val noteText = note(1);

							idx += 1;

							out << "<tr>";
							out << "<td valign=\"top\">";
							out < "<a class=\"note\" name=\"note_"+idx.toString()+"\">";
							out < noteCitation;
							out < "</a>";

							out << "</td><td valign=\"top\">";

							out < noteText;
							out < "</td>";
							out << "</tr>";
						}
						out << "</ol>";
					}
				}
			,TagName("text:note") > TagName("text:note-citation") > text
				enter { }
//			,TagName("text:note") > TagName("text:note-body") > paragraph
//				enter { }
//				exit { out << "<br />" }
		)
	}

	/**
	 * Отображение ссылок
	 */
	lazy val outputLinks = {
		List[NodePattern](
			hiperReference
				enter { out < "<a href=\""+attr("xlink:href")+"\">" }
				exit { out < "</a>" }
		)
	};

	/**
	 * Кодирует документа ODT (XML) в HTML
	 * @param node Корневой узел XML
	 * @return HTML документ
	 */
	def encode(node:XMLNode):String = {
		out.reset;
		_styles = new Styles(node);

		XMLVisitor.go(node,this);
		out.toString()
	}
}