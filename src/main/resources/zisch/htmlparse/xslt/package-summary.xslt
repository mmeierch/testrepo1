<?xml version="1.0"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns="https://dals.ch/ns/docml/1.0/">
  
  <xsl:include href="common.xslt"/>
  
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
  
  <xsl:variable name="packageName" select="normalize-space(/html:html/html:head/html:title)"/>

  <xsl:template match="/">
    <xsl:call-template name="page">
      <xsl:with-param name="" select="$packageName"/>
    </xsl:call-template>
    <xsl:text>

</xsl:text>
    <page>
      <xsl:copy-of select="namespace::*"/>
      <head>
        <title>Package <xsl:value-of select="$packageName"/></title>
        <navtitle><xsl:value-of select="$packageName"/></navtitle>
        <!-- TODO: Make sure that pages of the same order are sorted according to navtitle! -->
        <order>1</order>
      </head>
      <literal>
        <xsl:apply-templates select="/html:html/html:body/node()"/>
      </literal>
    </page>
  </xsl:template>
  
</xsl:stylesheet>
