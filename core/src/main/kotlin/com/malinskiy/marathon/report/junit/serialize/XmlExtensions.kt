package com.malinskiy.marathon.report.junit

import javax.xml.stream.XMLStreamWriter

inline fun XMLStreamWriter.document(init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartDocument("UTF-8", "1.0")
    this.init()
    this.writeEndDocument()
    return this
}

inline fun XMLStreamWriter.element(name: String, init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartElement(name)
    this.init()
    this.writeEndElement()
    return this
}

fun XMLStreamWriter.element(name: String, content: String) {
    element(name) {
        writeCharacters(content)
    }
}

fun XMLStreamWriter.attribute(name: String, value: String) = writeAttribute(name, value)

fun XMLStreamWriter.writeCDataSafely(data: String) = writeCData(data.replace("]]>", "]]]]><![CDATA[>"))
