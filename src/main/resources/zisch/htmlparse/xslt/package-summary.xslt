<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:docml="http://DOCML/">
  
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
  
  <xsl:variable name="packageName" select="normalize-space(/html:html/html:head/html:title)"/>

  <xsl:template match="/">
    <xsl:text>

</xsl:text>
    <docml:page>
      <docml:head>
        <docml:title>Package <xsl:value-of select="$packageName"/></docml:title>
      </docml:head>
      <docml:literal-html>
        <xsl:apply-templates select="/html:html/html:body/html:h2[1]/following-sibling::node()"/>
      </docml:literal-html>
    </docml:page>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
