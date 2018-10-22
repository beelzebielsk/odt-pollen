The idea here is to produce a basic language, in terms of xml/pollen
tags, that can readily be transformed to an ODT document. The point of
these elements is **presentation**. This is a presentation language.
It does not have to start out fancy. But, at minimum, it needs to be
able to output my resume.

A decent place to start might be conversion of a basic form of HTML to
ODT. HTML is close enough to what I'd want. There will be, perhaps, a
few minor additions to the elements to reflect the fact that these
documents have pages.

- Italic/Bold text. Should be usable in any element.
- Choose fonts for text.
- Tables
    - Set whether or not borders display (for now, all-or-nothing
      deal).
    - Set styles on individual cells
        - padding
        - text alignment
- Lists
    - Control indentation of items (one setting for all, or per-item
      settings are okay).
- Pages
    - margin sizes
    - decide where a new page starts
    - Global page settings which apply to every page
- Paragraphs of text
    - Set spacing before and after each paragraph.
    - Set indentation level of paragraph.

Format
======

The base format will be a SEXP which can either be converted to java
statements, or just converted to XML where a java program can process
it.

The SEXP format has the following elements, which can have the
following children:

- root
    - p
        - span
            - strings (not an element. Just strings of text)
        - bold
        - italic
    - ul
        - li
            - p
    - ol
        - li
            - p
    - table
        - row
            - cell
                - p

Approaches
==========

Map Elements to Simple API Objects
----------------------------------

This is one option. I can make the pollen SEXP a simpler way of
expressing a series of procedural Simple API calls, while glossing
over some of the things that the Simple API doesn't make so simple.

To make this easier, it would be nice if I could always dip from the
Simple API to the ODFDOM level, and create/add nodes this way. Because
creation of spans is not always easy, and that's ultimately how I'm
going to be adding text.

Styling could be done by creating styles and then putting a reference
to the style, much like how the actual ODF file does it, but with
human-readable names.

Some tags could just make stuff that should feel easy a little
simpler.

You know... translating from Racket to Java is sort of like RPC.

Stretch Goals
=============

- Per-page settings, so that each new page can have potentially
  different sizes and stuff.
- Establish fallback/default fonts/attributes, much like how CSS does
  it.
- Choose text color.
- Both ordered and unordered lists.
- Choose bullet character for unordered lists.
- the attributes of an element will become attributes from the ODF
  specification. This way I don't have to make my own attribute set.
