<?xml version="1.0" ?>
<!--
Copyright (c) 2009 University of Tartu
-->
<xsl:stylesheet version="1.0" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="xsd:schema">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:call-template name="TargetValueStats"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template name="extension">
		<xsd:element ref="Extension" minOccurs="0" maxOccurs="unbounded"/> 
	</xsl:template>

	<xsl:template name="extension-comment">
		<xsl:comment> &lt;xs:element ref=&quot;Extension&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt; </xsl:comment>
	</xsl:template>

	<!-- 
	Model types have one Extension list in the beginning and another Extension list in the end, which is too complex for the XJC to handle.
	-->
	<xsl:template match="xsd:element[@ref='Extension'][position() &gt; 1 and position() = last()]">
		<xsl:call-template name="extension-comment"/>
	</xsl:template>

	<xsl:template match="xsd:group[@name='EmbeddedModel']/xsd:sequence/xsd:element[@ref='Extension']">
		<xsl:call-template name="extension-comment"/>
	</xsl:template>

	<!--
	Simplify Array type definition
	-->
	<xsl:template match="xsd:element[@name='Array']">
	</xsl:template>

	<xsl:template match="xsd:complexType[@name='ArrayType']">
		<xsl:element name="xsd:element">
			<xsl:attribute name="name">Array</xsl:attribute>
			<xsl:element name="xsd:complexType">
				<xsl:element name="xsd:simpleContent">
					<xsl:element name="xsd:extension">
						<xsl:attribute name="base">xsd:string</xsl:attribute>
						<xsl:copy-of select="*"/>
					</xsl:element>
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<!--
	The type of Field name needs to be restricted from xsd:string to FIELD-NAME. Here, an XSL transformation feels more elegant than a property-level JAXB customization.
	-->
	<xsl:template match="xsd:element[@name='ParameterField']/xsd:complexType/xsd:attribute[@name='name']/@type">
		<xsl:attribute name="type">FIELD-NAME</xsl:attribute>
	</xsl:template>

	<!--
	Simplify CONTINUOUS-DISTRIBUTION-TYPE group definition from XSD sequence to XSD choice by relocating the Extension element.
	-->
	<xsl:template match="xsd:group[@name='CONTINUOUS-DISTRIBUTION-TYPES']">
		<xsl:copy>
			<xsl:apply-templates select="@*|xsd:sequence/xsd:choice"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="xsd:element[@name='Alternate']/xsd:complexType">
		<xsl:copy>
			<xsd:sequence>
				<xsl:call-template name="extension"/>
				<xsl:apply-templates select="@*|node()"/>
			</xsd:sequence>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="xsd:element[@name='Baseline']/xsd:complexType">
		<xsl:copy>
			<xsd:sequence>
				<xsl:call-template name="extension"/>
				<xsl:apply-templates select="@*|node()"/>
			</xsd:sequence>
		</xsl:copy>
	</xsl:template>

	<!--
	Extending the Naive Bayes Model Element in PMML: Adding Support for Continuous Input Variables (http://kdd13pmml.files.wordpress.com/2013/07/guazzelli_et_al.pdf)
	-->
	<xsl:template match="xsd:element[@name='BayesInput']/xsd:complexType/xsd:sequence">
		<xsl:copy>
			<xsl:apply-templates select="xsd:element[@ref='Extension' or @ref='DerivedField']"/>
			<xsd:element ref="TargetValueStats" minOccurs="0" maxOccurs="1"/>
			<xsl:apply-templates select="xsd:element[@ref='PairCounts']"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template name="TargetValueStats">
		<xsd:element name="TargetValueStats">
			<xsd:complexType>
				<xsd:sequence>
					<xsd:element ref="Extension" minOccurs="0" maxOccurs="unbounded"/>
					<xsd:element ref="TargetValueStat" maxOccurs="unbounded" />
				</xsd:sequence>
			</xsd:complexType>
		</xsd:element>
		<xsd:element name="TargetValueStat">
			<xsd:complexType>
				<xsd:sequence>
					<xsd:element ref="Extension" minOccurs="0" maxOccurs="unbounded"/>
					<xsd:group ref="CONTINUOUS-DISTRIBUTION-TYPES" minOccurs="1"/>
				</xsd:sequence>
				<xsd:attribute use="required" type="xs:string" name="value"/>
			</xsd:complexType>
		</xsd:element>
	</xsl:template>
</xsl:stylesheet>