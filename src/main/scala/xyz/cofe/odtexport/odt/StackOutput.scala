package xyz.cofe.odtexport.odt

/**
 * Класс потока - вывода символьной информации со стеком.  
 */
class StackOutput {
	import scala.collection.immutable.Stack;
	
	private var outSB = new StringBuilder;
	private var outSBStack = Stack[StringBuilder]();
	
	/**
	 * Указывает символ(ы) перевода строки
	 */
	def newline : String = "\n";
	
	/**
	 * Указывает текст который выводится если передана пустая ссылка
	 * 
	 */
	def nullText : String = "null";

	/**
	 * Выводит текст дополняя в конце символом перевода строки
	 * @param text Текст который необходимо вывести
	 * @return Этот же самый поток вывода 
	 */
	def << (text:String):StackOutput = {outSB.append(text); outSB.append(newline); this}
	
	/**
	 * Выводит текст дополняя в конце символом перевода строки
	 * @param any Текст который необходимо вывести
	 * @return Это тже самый поток вывода
	 */
	def << (any:Any):StackOutput = {outSB.append(any); outSB.append(newline); this}

	/**
	 * Выводит текст
	 * @param text Текст который необходимо вывести
	 * @return Этот же самый поток вывода
	 */
	def < (text:String):StackOutput = {outSB.append(text);this}
	
	/**
	 * Выводит текст
	 * @param any Текст который необходимо вывести
	 * @return Этот же самый поток вывода
	 */
	def < (any:Any):StackOutput = {outSB.append(any);this}
	
	/**
	 * Сохраняет буфер в стеке, затем буфер очищается. 
	 */
	def push():Unit = { 
		outSBStack = outSBStack.push(outSB);
		outSB = new StringBuilder;
	}
	
	/**
	 * 1) Текущее буфер возвращает как результат. 
	 * 2) Востанавливает ранее сохраненный текст из стека и заменяет им текущий буфер.
	 * @return Содержимое буфера до восстановления из стека
	 */
	def pop():String = {
		if( outSBStack.size<1 )return outSB.toString();
		val res = outSB.toString();
		outSB = outSBStack.top;
		outSBStack.pop;
		return res;
	}

	/**
	 * Сброс состояния потока вместе со стеком
	 */
	def reset : Unit = {
		outSB.clear;
		outSBStack = Stack[StringBuilder]();
	}
	
	override def toString() : String = outSB.toString();
}