/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.xjc;

import java.util.*;

import com.sun.codemodel.*;
import com.sun.tools.xjc.*;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.*;
import com.sun.tools.xjc.util.*;
import com.sun.xml.bind.api.impl.*;

import org.xml.sax.*;

public class VisitorPlugin extends Plugin {

	@Override
	public String getOptionName(){
		return "Xvisitor";
	}

	@Override
	public String getUsage(){
		return null;
	}

	@Override
	public boolean run(Outline outline, Options options, ErrorHandler errorHandler){
		JCodeModel codeModel = outline.getCodeModel();

		CodeModelClassFactory clazzFactory = outline.getClassFactory();

		JClass objectClazz = codeModel.ref(Object.class);

		JClass pmmlObjectClazz = codeModel.ref("org.dmg.pmml.PMMLObject");
		JClass visitableInterface = codeModel.ref("org.dmg.pmml.Visitable");

		JPackage modelPackage = pmmlObjectClazz._package();

		JDefinedClass visitorActionClazz = clazzFactory.createClass(modelPackage, JMod.PUBLIC, "VisitorAction", null, ClassType.ENUM);
		JEnumConstant continueAction = visitorActionClazz.enumConstant("CONTINUE");
		JEnumConstant skipAction = visitorActionClazz.enumConstant("SKIP");
		JEnumConstant terminateAction = visitorActionClazz.enumConstant("TERMINATE");

		JDefinedClass visitorInterface = clazzFactory.createClass(modelPackage, JMod.PUBLIC, "Visitor", null, ClassType.INTERFACE);

		JDefinedClass abstractVisitorClazz = clazzFactory.createClass(modelPackage, JMod.ABSTRACT | JMod.PUBLIC, "AbstractVisitor", null, ClassType.CLASS)._implements(visitorInterface);
		JDefinedClass abstractSimpleVisitorClazz = clazzFactory.createClass(modelPackage, JMod.ABSTRACT | JMod.PUBLIC, "AbstractSimpleVisitor", null, ClassType.CLASS)._implements(visitorInterface);

		JMethod defaultMethod = abstractSimpleVisitorClazz.method(JMod.PUBLIC, visitorActionClazz, "visit");
		defaultMethod.param(pmmlObjectClazz, "object");
		defaultMethod.body()._return(continueAction);

		Set<JType> traversableTypes = new LinkedHashSet<JType>();

		Collection<? extends ClassOutline> clazzes = outline.getClasses();
		for(ClassOutline clazz : clazzes){
			JDefinedClass beanClazz = clazz.implClass;
			traversableTypes.add(beanClazz);

			JClass beanSuperClazz = beanClazz._extends();
			traversableTypes.add(beanSuperClazz);
		} // End for

		for(ClassOutline clazz : clazzes){
			JDefinedClass beanClazz = clazz.implClass;

			String parameterName = NameConverter.standard.toVariableName(beanClazz.name());
			if(!JJavaName.isJavaIdentifier(parameterName)){
				parameterName = ("_" + parameterName);
			}

			JMethod visitorVisit = visitorInterface.method(JMod.PUBLIC, visitorActionClazz, "visit");
			visitorVisit.param(beanClazz, parameterName);

			JMethod abstractVisitorVisit = abstractVisitorClazz.method(JMod.PUBLIC, visitorActionClazz, "visit");
			abstractVisitorVisit.annotate(Override.class);
			abstractVisitorVisit.param(beanClazz, parameterName);
			abstractVisitorVisit.body()._return(continueAction);

			JClass beanSuperClass = beanClazz._extends();

			JMethod abstractSimpleVisitorVisit = abstractSimpleVisitorClazz.method(JMod.PUBLIC, visitorActionClazz, "visit");
			abstractSimpleVisitorVisit.annotate(Override.class);
			abstractSimpleVisitorVisit.param(beanClazz, parameterName);
			abstractSimpleVisitorVisit.body()._return(JExpr.invoke(defaultMethod).arg(JExpr.cast(beanSuperClass, JExpr.ref(parameterName))));

			JMethod beanAccept = beanClazz.method(JMod.PUBLIC, visitorActionClazz, "accept");
			beanAccept.annotate(Override.class);

			JVar visitorParameter = beanAccept.param(visitorInterface, "visitor");

			JBlock body = beanAccept.body();

			JVar status = body.decl(visitorActionClazz, "status", JExpr.invoke(visitorParameter, "visit").arg(JExpr._this()));

			FieldOutline[] fields = clazz.getDeclaredFields();
			for(FieldOutline field : fields){
				CPropertyInfo propertyInfo = field.getPropertyInfo();

				String fieldName = propertyInfo.getName(false);

				JFieldRef fieldRef = JExpr.refthis(fieldName);

				JType fieldType = field.getRawType();

				// Collection of values
				if(propertyInfo.isCollection()){
					JType fieldElementType = CodeModelUtil.getElementType(fieldType);

					if(traversableTypes.contains(fieldElementType) || objectClazz.equals(fieldElementType)){
						JForLoop forLoop = body._for();
						JVar var = forLoop.init(codeModel.INT, "i", JExpr.lit(0));
						forLoop.test((status.eq(continueAction)).cand(fieldRef.ne(JExpr._null())).cand(var.lt(fieldRef.invoke("size"))));
						forLoop.update(var.incr());

						JExpression getElement = (JExpr.invoke(fieldRef, "get")).arg(var);

						if(traversableTypes.contains(fieldElementType)){
							forLoop.body().assign(status, getElement.invoke("accept").arg(visitorParameter));
						} else

						if(objectClazz.equals(fieldElementType)){
							forLoop.body()._if(getElement._instanceof(visitableInterface))._then().assign(status, ((JExpression)JExpr.cast(visitableInterface, getElement)).invoke("accept").arg(visitorParameter));
						}
					}
				} else

				// Simple value
				{
					if(traversableTypes.contains(fieldType)){
						body._if((status.eq(continueAction)).cand(fieldRef.ne(JExpr._null())))._then().assign(status, JExpr.invoke(fieldRef, "accept").arg(visitorParameter));
					}
				}
			}

			body._if(status.eq(terminateAction))._then()._return(terminateAction);

			body._return(continueAction);
		}

		return true;
	}
}