package com.loraneo.tools.commands;

import java.util.Optional;

import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import com.loraneo.tools.commands.category.JavaUtilsCategory;
import com.loraneo.tools.commands.functional.Try;

public class JavaBuilderGenerator extends AbstractUICommand {

  @Override
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata.forCommand(JavaBuilderGenerator.class)
        .name("Generate builder")
        .category(JavaUtilsCategory.instance());
  }

  @Override
  public void initializeUI(UIBuilder builder) throws Exception {}

  @Override
  public Result execute(UIExecutionContext context) throws Exception {
    Optional.of(context)
        .map(UIExecutionContext::getUIContext)
        .map(UIContext::getInitialSelection)
        .map(UISelection::get)
        .flatMap(this::getJavResource)
        .ifPresent(p -> p.setContents(this.generateBuilder(p)));
    return Results.success("Created builder");
  }

  private Optional<JavaResource> getJavResource(Object resource) {
    return Optional.of(resource)
        .filter(p -> JavaResource.class.isAssignableFrom(p.getClass()))
        .map(p -> (JavaResource) p);
  }

  private JavaClassSource generateBuilder(JavaResource resource) {
    return Try.<JavaClassSource>mapTry(resource::getJavaType).map(p -> addBuilder(p)).get();
  }

  private JavaClassSource addBuilder(JavaClassSource resource) {
    Optional.of(resource).map(this::clearContent);

    resource.getFields().stream().forEach(p -> this.createGetter(resource, p));

    resource
        .addMethod()
        .setPublic()
        .setStatic(true)
        .setName("builder")
        .setReturnType("Builder")
        .setBody("return new " + resource.getName() + "().new Builder();");

    JavaClassSource neastedType = resource.addNestedType("public class Builder {} ");

    resource
        .getFields()
        .stream()
        .forEach(
            p -> this.createBuildMethod(resource, neastedType, p.getName(), p.getType().getName()));
    createBuildMethod(resource, neastedType);
    
    resource.removeImport(neastedType);
    return resource;
  }

  private void createGetter(JavaClassSource target, FieldSource<JavaClassSource> field) {
    target
        .addMethod()
        .setPublic()
        .setStatic(false)
        .setName("get" + toCamel(field))
        .setReturnType(field.getType().getQualifiedNameWithGenerics())
        .setBody("return this." + field.getName() + ";");
  }

  private String toCamel(FieldSource<JavaClassSource> field) {
    return field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
  }

  private JavaClassSource clearContent(JavaClassSource resource) {
    resource.getMethods().forEach(p -> resource.removeMethod(p));
    resource.getNestedTypes().forEach(p -> resource.removeNestedType(p));
    return resource;
  }

  private void createBuildMethod(
      JavaClassSource original, JavaClassSource target, String fieldNane, String type) {
    target
        .addMethod()
        .setPublic()
        .setStatic(false)
        .setName(fieldNane)
        .setReturnType(target.getName())
        .setBody(original.getName() + ".this." + fieldNane + " = " + fieldNane + ";\n return this;")
        .addParameter(type, fieldNane);
  }

  private void createBuildMethod(JavaClassSource original, JavaClassSource target) {
    target
        .addMethod()
        .setPublic()
        .setStatic(false)
        .setName("build")
        .setReturnType(original.getName())
        .setBody("return " + original.getName() + ".this;\n");
  }
}
