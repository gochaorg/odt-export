package xyz.cofe.odtexport.odt

/**
 * Набор специфичных для odt  документа (xml - content.xml) XTest шаблонов
 */
trait ODTXMLPatterns
{
	import xyz.cofe.odtexport.xtest._;
	import xyz.cofe.odtexport.odt.style.Styles;
	import xyz.cofe.odtexport.odt.xtest.StyleName;
	import xyz.cofe.odtexport.odt.xtest.StyleProperty;

	/**
	 * Здесь будут содержаться стили для текущего обрабатываемого документа
	 */
	def styles : Styles;

	/**
	 * Работа со стилями ODT
	 */
	object style
	{
		/**
		 * Указывает стиль текста/параграфа
		 * @param name Имя стиля
		 */
		def name( name:String ):XTest = new StyleName(styles, name);

		/**
		 * Определяет имя свойства
		 * @param name Имя свойства
		 * @return Наблор условий для проверки
		 */
		def property( name:String* ) = new StyleProperty(styles,name:_*);

		/**
		 * Указывает на отсуствие обтекания картинки
		 */
		def noWrap = style.property( "style:graphic-properties","style:wrap" ) == "none";
	}

	/**
	 * Текст ODT
	 */
	def text = TagName("#text");

	/**
	 * Символ табуляции
	 */
	def tab = TagName("text:tab");

	/**
	 * Заголовок текста
	 */
	def textHeader = TagName("text:h");

	/**
	 * Указывает параграф
	 */
	def paragraph = TagName("text:p");

	/**
	 * Разрыв линии, переход на след. линию параграфа
	 */
	def lineBreak = TagName("text:line-break");

	/**
	 * Разрыв страницы
	 */
	def softBreak = TagName("text:soft-page-break");

	/**
	 * Работа с закладками и перекрестными ссылками
	 */
	object bookmark
	{
		/**
		 * Начало закладки
		 */
		def start = TagName("text:bookmark") +@ "text:name";

		/**
		 * Конец закладки
		 */
		def end = TagName("text:bookmark-end");

		/**
		 * Ссылка на закладку
		 */
		def ref = TagName("text:bookmark-ref") +@ "text:ref-name";
	}

	/**
	 * Работа с закладками и перекрестными ссылками
	 */
	object textSequence
	{
		/**
		 * Начало закладки на элемент (таблцу,...)
		 */
		def sequence = TagName("text:sequence") +@ "text:ref-name";

		/**
		 * Ссылка на закладку
		 */
		def ref = TagName("text:sequence-ref") +@ "text:ref-name";
	}

	/**
	 * Работа с закладками и перекрестными ссылками
	 */
	object textMark
	{
		/**
		 * Начало закладки
		 */
		def start = TagName("text:reference-mark-start") +@ "text:name";

		/**
		 * Конец закладки
		 */
		def end = TagName("text:reference-mark-end");

		/**
		 * Ссылка на закладку
		 */
		def ref = TagName("text:reference-ref") +@ "text:ref-name";
	}

	/**
	 * Ссылка в тексте
	 */
	def hiperReference = TagName("text:a") +@ "xlink:href";

	/**
	 * Привязка "якоря"
	 */
	object anchor
	{
		/**
		 * Врезка / картика как символ
		 */
		def asChar = Attribute("text:anchor-type") == "as-char";

		/**
		 * Врезка / картика как символ
		 */
		def toParagraph = Attribute("text:anchor-type") == "paragraph";
	}

	/**
	 * Списоки
	 */
	object list
	{
		/**
		 * Список
		 */
		def list = TagName("text:list");

		/**
		 * Нумерованный список
		 */
		def orderedList = list & style.property("text:list-level-style-number").exists;

		/**
		 * Маркерованный список
		 */
		def unorderedList =	list & style.property("text:list-level-style-bullet").exists;

		/**
		 * Элемент списка
		 */
		def item = TagName("text:list-item");
	}

	/**
	 * Картинка
	 */
	object image
	{
		/**
		 * Картинка
		 */
		def image = TagName("draw:image") +@ "xlink:href";

		/**
		 * Фрейм картинки с размерами
		 */
		def frame = TagName("draw:frame") +@ "svg:width" +@ "svg:height" & style.name( "Graphics" );
	}

	/**
	 * Выравнивание
	 */
	object align
	{
		/**
		 * По горизонтали
		 */
		object horz
		{
			/**
			 * Выравнивание по центру - граф. свойство стиля
			 */
			def center = style.property( "style:graphic-properties","style:horizontal-pos" ) == "center";

			/**
			 * Выравнивание слева - граф. свойство стиля
			 */
			def left = style.property( "style:graphic-properties","style:horizontal-pos" ) == "left";

			/**
			 * Выравнивание справа - граф. свойство стиля
			 */
			def right = (
					style.property( "style:graphic-properties","style:horizontal-pos" ) == "right"
				) | (
					style.property( "style:graphic-properties","style:horizontal-pos" ) == "from-left"
				);
		}

		/**
		 * По вертикали
		 */
		object vert
		{
			/**
			 * Вертикальное выравнивание по центру
			 */
			def center =
				style.property( "style:graphic-properties","style:vertical-pos" ) == "middle";

			/**
			 * Вертикальное выравнивание по низу
			 */
			def bottom =
				style.property( "style:graphic-properties","style:vertical-pos" ) == "bottom";

			/**
			 * Вертикальное выравнивание по верху
			 */
			def top =
				(
					style.property( "style:graphic-properties","style:vertical-pos" ) == "top"
				) | (
					style.property( "style:graphic-properties","style:vertical-pos" ) == "from-top"
				);
		}
	}

	/**
	 * Врезка
	 */
	def incut = TagName("draw:frame") & style.name ( "Frame" );

	/**
	 * Таблицы
	 */
	object table
	{
		/**
		 * Указывает таблицу
		 */
		def table = TagName("table:table");

		/**
		 * Указывает загаловок таблицы
		 */
		def headerRow = TagName("table:table-header-rows") >> TagName("table:table-row");

		/**
		 * Указывает строку таблицы
		 */
		def row = TagName("table:table-row");

		/**
		 * Указывает ячейку таблицы
		 */
		def cell = TagName("table:table-cell");

		def cellWithColSpan = cell +@ "table:number-columns-spanned";
		def cellWithRowSpan = cell +@ "table:number-rows-spanned";
		def cellWithRowColSpan = cell+@ "table:number-columns-spanned" +@ "table:number-rows-spanned";
	}
}