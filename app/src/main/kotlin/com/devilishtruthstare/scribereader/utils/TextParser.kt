package com.devilishtruthstare.scribereader.utils

sealed class NodeElement(
    open val tagName: String,
    open val parent: NodeElement?,
    open val attributes: MutableMap<String, String>,
    open val children: MutableList<NodeElement>) {

    data class TextElement(
        override val tagName: String,
        override val parent: NodeElement?,
        override val attributes: MutableMap<String, String>,
        var text: String,
    ) : NodeElement(tagName, parent, mutableMapOf(), mutableListOf())

    data class Node(
        override val tagName: String,
        override val parent: NodeElement?,
        override val attributes: MutableMap<String, String>,
    ) : NodeElement(tagName, parent, mutableMapOf(), mutableListOf())
}
// body - permitted parents (html) (must be second element)
// caption - The end tag can be omitted if the element is not immediately followed by ASCII whitespace or a comment
// colgroup -  The start tag may be omitted, if it has a <col> element as its first child and if it is not preceded by a <colgroup> whose end tag has been omitted. The end tag may be omitted, if it is not followed by a space or a comment.
// dd - The start tag is required. The end tag may be omitted if this element is immediately followed by another <dd> element or a <dt> element, or if there is no more content in the parent element.
// dt - The start tag is required. The end tag may be omitted if this element is immediately followed by another <dt> element or a <dd> element, or if there is no more content in the parent element.
// head -  If the document is an <iframe> srcdoc document, or if title information is available from a higher level protocol (like the subject line in HTML email), zero or more elements of metadata content. Otherwise, one or more elements of metadata content where exactly one is a <title> element.
// html - The start tag may be omitted if the first thing inside the <html> element is not a comment. The end tag may be omitted if the <html> element is not immediately followed by a comment.
// li - The end tag can be omitted if the list item is immediately followed by another <li> element, or if there is no more content in its parent element.
// optgroup - The start tag is mandatory. The end tag is optional if this element is immediately followed by another <optgroup> element, or if the parent element has no more content.
// option - The start tag is mandatory. The end tag is optional if this element is immediately followed by another <option> element or an <optgroup>, or if the parent element has no more content.
// p - The start tag is required. The end tag may be omitted if the <p> element is immediately followed by an <address>, <article>, <aside>, <blockquote>, <details>, <div>, <dl>, <fieldset>, <figcaption>, <figure>, <footer>, <form>, h1, h2, h3, h4, h5, h6, <header>, <hgroup>, <hr>, <main>, <menu>, <nav>, <ol>, <pre>, <search>, <section>, <table>, <ul> or another <p> element, or if there is no more content in the parent element and the parent element is not an <a>, <audio>, <del>, <ins>, <map>, <noscript> or <video> element, or an autonomous custom element.
// rp - The end tag can be omitted if the element is immediately followed by an <rt> or another <rp> element, or if there is no more content in the parent element.
// rt - The end tag may be omitted if the <rt> element is immediately followed by an <rt> or <rp> element, or if there is no more content in the parent element
// tbody - A <tbody> element's start tag can be omitted if the first thing inside the <tbody> element is a <tr> element, and if the element is not immediately preceded by a <tbody>, <thead>, or <tfoot> element whose end tag has been omitted. (It can't be omitted if the element is empty.) A <tbody> element's end tag can be omitted if the <tbody> element is immediately followed by a <tbody> or <tfoot> element, or if there is no more content in the parent element.
// td - The start tag is mandatory. The end tag may be omitted, if it is immediately followed by a <th> or <td> element or if there are no more data in its parent element.
// tfoot - The start tag is mandatory. The end tag may be omitted if there is no more content in the parent <table> element.
// th - The start tag is mandatory. The end tag may be omitted, if it is immediately followed by a <th> or <td> element or if there are no more data in its parent element.
// thead - The start tag is mandatory. The end tag may be omitted if the <thead> element is immediately followed by a <tbody> or <tfoot> element.
// tr - Start tag is mandatory. End tag may be omitted if the <tr> element is immediately followed by a <tr> element, or if the row is the last element in its parent table group (<thead>, <tbody> or <tfoot>) element.

val tagClassNoEnd = setOf(
    "area",
    "base", "br",
    "col",
    "embed",
    "hr",
    "img", "input",
    "link",
    "meta",
    "track",
    "wbr"
)

fun TraverseNodes(text: String): NodeElement {
    var mode = "TEXT"

    var buffer = ""
    var tagName = ""
    var attributes = ""

    var isClosingTag = false
    var workingOnTagName = true
    var isComment = false

    var root = NodeElement.Node(
        tagName = "root",
        parent = null,
        attributes = mutableMapOf()
    )

    var currentNode: NodeElement = root

    text.forEachIndexed { index, char ->
        when (mode) {
            "TEXT" -> {
                if (char == '<') {
                    mode = "TAG"

                    if (!buffer.isEmpty()) {
                        currentNode.children.add(
                            NodeElement.TextElement(
                                tagName = "TEXT",
                                parent = currentNode,
                                text = buffer,
                                attributes = mutableMapOf()
                            )
                        )
                        buffer = ""
                    }

                    if (text.length-1 > index && text.get(index+1) == '/') {
                        isClosingTag = true
                    }

                    workingOnTagName = true
                }
            }
            "TAG" -> {
                if (char == '>') {
                    if (isComment) {

                    }
                    if (isClosingTag) {
                        if (currentNode.parent != null) {
                            currentNode = currentNode.parent!!
                        }
                    } else if ((index > 0 && text.get(index-1) == '/') || tagName in tagClassNoEnd) {
                        // Self contained node
                        var tag = NodeElement.Node(
                            tagName = tagName,
                            parent = currentNode,
                            attributes = mutableMapOf()
                        )
                        currentNode.children.add(tag)
                    } else {
                        // Starting Tag
                        var tag = NodeElement.Node(
                            tagName = tagName,
                            parent = currentNode,
                            attributes = mutableMapOf()
                        )
                        currentNode.children.add(tag)
                        currentNode = tag
                    }

                    mode = "TEXT"
                    isClosingTag = false
                    tagName = ""
                    attributes = ""
                    buffer = ""
                    isComment = false
                } else if (char == ' ' && workingOnTagName) {
                    workingOnTagName = false
                    if (tagName == "!--") {
                        isComment = true
                    }
                } else if (workingOnTagName) {
                    tagName += char
                } else {
                    attributes += char
                }
            }
        }
    }

    return root
}