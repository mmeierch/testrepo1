<?xml version="1.0"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns="https://dals.ch/ns/docml/1.0/">
  
  <!-- Skeleton for a complete page: -->
  <xsl:template name="page">
    <xsl:param name="type" select=""/>
    <xsl:param name="name" select=""/>
    
    <xsl:text>

</xsl:text>
    <page>
      <xsl:copy-of select="namespace::*"/>
      <head>
        <title><xsl:value-of select="$type"/> <xsl:value-of select="$name"/></title>
        <navtitle><xsl:value-of select="$name"/></navtitle>
        <!-- TODO: Make sure that pages of the same order are sorted according to navtitle! -->
        <order>1</order>
      </head>
      <literal>
        <xsl:apply-templates select="/html:html/html:body/node()"/>
      </literal>
    </page>
  </xsl:template>
  
  <!-- Remove elements and fragments from the Javadoc pages which we don't want in our result: -->
  <xsl:template match="html:script|html:noscript|html:div[@class='subNav']">
    <!-- remove these elements from the output -->
  </xsl:template>

  <!-- Copy HTML elements, changing the namespace prefix from default to 'html': -->  
  <xsl:template match="html:*">
    <xsl:element name="html:{local-name()}">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="node()"/>
   </xsl:element>
  </xsl:template>

  <!-- Identity transformation for all nodes which are not matched by other nodes: -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
