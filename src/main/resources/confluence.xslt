<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" encoding="UTF-8" indent="yes"
		cdata-section-elements="body title" />

	<xsl:template match="/">
		<spaces>
			<xsl:apply-templates select="//object[@class='Space']" />
		</spaces>
	</xsl:template>

	<xsl:template match="object[@class='Space']">
		<space>
			<xsl:attribute name="id">
        <xsl:value-of select="id" />
      </xsl:attribute>
			<xsl:attribute name="name">
        <xsl:value-of select="property[@name='name']" />
      </xsl:attribute>
			<xsl:attribute name="key">
        <xsl:value-of select="property[@name='key']" />
      </xsl:attribute>
			<xsl:variable name="pageId" select="property[@name='homePage']/id" />
			<xsl:if test="$pageId">
				<xsl:apply-templates select="//object[@class='Page' and id/text()=$pageId]" />
			</xsl:if>
		</space>
	</xsl:template>

	<xsl:template match="object[@class='Page']">
		<page>
			<xsl:attribute name="id">
        <xsl:value-of select="id" />
      </xsl:attribute>
			<xsl:attribute name="creator">
        <xsl:call-template name="userName">
          <xsl:with-param name="userKey"
				select="property[@name='creator']/id/text()" />
        </xsl:call-template>
      </xsl:attribute>
			<xsl:attribute name="lastModifier">
        <xsl:call-template name="userName">
          <xsl:with-param name="userKey"
				select="property[@name='lastModifier']/id/text()" />
        </xsl:call-template>
      </xsl:attribute>
			<xsl:attribute name="creationDate">
        <xsl:value-of select="property[@name='creationDate']" />
      </xsl:attribute>
			<xsl:attribute name="lastModificationDate">
        <xsl:value-of select="property[@name='lastModificationDate']" />
      </xsl:attribute>
			<title>
				<xsl:value-of select="property[@name='title']" />
			</title>
			<xsl:apply-templates select="collection[@name='bodyContents']" />
			<xsl:apply-templates select="collection[@name='attachments']" />
			<xsl:apply-templates select="collection[@name='children']" />
		</page>
	</xsl:template>

	<xsl:template match="object[@class='Attachment']">
		<xsl:if test="not(property[@name='originalVersion']/id)">
			<attachment>
				<xsl:attribute name="id">
          <xsl:value-of select="id" />
        </xsl:attribute>
				<xsl:attribute name="fileName">
          <xsl:value-of select="property[@name='fileName']" />
        </xsl:attribute>
				<xsl:attribute name="contentType">
          <xsl:value-of select="property[@name='contentType']" />
        </xsl:attribute>
				<xsl:attribute name="version">
          <xsl:value-of select="property[@name='attachmentVersion']" />
        </xsl:attribute>
				<xsl:attribute name="fileSize">
          <xsl:value-of select="property[@name='fileSize']" />
        </xsl:attribute>
				<xsl:attribute name="pageId">
          <xsl:value-of select="property[@name='content']/id" />
        </xsl:attribute>
			</attachment>
		</xsl:if>
	</xsl:template>

	<xsl:template match="object[@class='BodyContent']">
		<body>
			<xsl:attribute name="id">
        <xsl:value-of select="id" />
      </xsl:attribute>
			<xsl:attribute name="bodyType">
        <xsl:value-of select="property[@name='bodyType']" />
      </xsl:attribute>
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="property[@name='body']/text()" />
				<xsl:with-param name="replace" select="']] >'" />
				<xsl:with-param name="by" select="']]>'" />
			</xsl:call-template>
		</body>
	</xsl:template>

	<xsl:template match="collection[@name='bodyContents']">
		<bodies>
			<xsl:for-each select="element[@class='BodyContent']/id">
				<xsl:variable name="bodyContentId" select="text()" />
				<xsl:apply-templates
					select="//object[@class='BodyContent' and id/text()=$bodyContentId]" />
			</xsl:for-each>
		</bodies>
	</xsl:template>

	<xsl:template match="collection[@name='attachments']">
		<attachments>
			<xsl:for-each select="element[@class='Attachment']/id">
				<xsl:variable name="attachmentId" select="text()" />
				<xsl:apply-templates
					select="//object[@class='Attachment' and id/text()=$attachmentId]" />
			</xsl:for-each>
		</attachments>
	</xsl:template>

	<xsl:template match="collection[@name='children']">
		<children>
			<xsl:for-each select="element[@class='Page']/id">
				<xsl:variable name="pageId" select="text()" />
				<xsl:apply-templates select="//object[@class='Page' and id/text()=$pageId]" />
			</xsl:for-each>
		</children>
	</xsl:template>

	<xsl:template name="userName">
		<xsl:param name="userKey" />
		<xsl:variable name="userId"
			select="//object[@class='ConfluenceUserImpl' and id/text()=$userKey]/property[@name='name']" />
		<xsl:value-of
			select="//object[@class='InternalUser' and property[@name='name' and text()=$userId]]/property[@name='displayName']" />
	</xsl:template>

	<xsl:template name="string-replace-all">
		<xsl:param name="text" />
		<xsl:param name="replace" />
		<xsl:param name="by" />
		<xsl:choose>
			<xsl:when test="contains($text, $replace)">
				<xsl:value-of select="substring-before($text,$replace)" />
				<xsl:value-of select="$by" />
				<xsl:call-template name="string-replace-all">
					<xsl:with-param name="text"
						select="substring-after($text,$replace)" />
					<xsl:with-param name="replace" select="$replace" />
					<xsl:with-param name="by" select="$by" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
