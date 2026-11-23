package com.snhu.mscs.cs600.module5;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.StreamResource;

/**
 * MainLayout -
 *
 * Purpose:
 *      Provides the shared application layout, including the top
 *      header and side navigation drawer.
 *
 * Expected Results:
 *      Views using this layout display a consistent header and
 *      navigation structure.
 *
 * Version              Author                  Date            Description
 * ***********************************************************************
 *     1                 Christopher Bright      21-Oct-2026     Initial Creation
 *     2                 Christopher Bright      21-Oct-2026     Updated UI styling
 *     3                 Christopher Bright      21-Oct-2026     Updated PNG logo
 */
public class MainLayout extends AppLayout {

  private static final String PRIMARY_COLOR = "#172235";
  private static final String ACCENT_COLOR = "#327e91";
  private static final String BORDER_COLOR = "#d9e1e8";

  /**
   * Builds the shared application layout.
   */
  public MainLayout() {
    setPrimarySection(Section.DRAWER);

    addToNavbar(createHeader());
    addToDrawer(createDrawer());

    // Keep the drawer wide enough for navigation labels without
    // taking unnecessary space away from the main content.
    getElement().getStyle()
        .set("--vaadin-app-layout-drawer-width", "220px");
  }

  /**
   * Creates the header shown across the top of the application.
   *
   * @return configured header component
   */
  private Component createHeader() {

    DrawerToggle drawerToggle = new DrawerToggle();

    drawerToggle.getStyle()
        .set("color", PRIMARY_COLOR)
        .set("margin-right", "4px");

    // Load the logo from the application's classpath resources.
    StreamResource logoResource = new StreamResource(
        "module5-logo.png",
        () -> getClass().getResourceAsStream("/module5-logo.png")
    );

    Image logo = new Image(
        logoResource,
        "Module5 Logo"
    );

    logo.setWidth("42px");
    logo.setHeight("42px");

    // Preserve the source image proportions inside the fixed logo area.
    logo.getStyle()
        .set("object-fit", "contain");

    Span applicationName = new Span("Module5");

    applicationName.getStyle()
        .set("font-size", "1.25rem")
        .set("font-weight", "700")
        .set("color", PRIMARY_COLOR);

    Span developerName = new Span("Chris Bright");

    developerName.getStyle()
        .set("font-size", "0.95rem")
        .set("font-weight", "500")
        .set("color", "#475569")
        .set("margin-left", "auto");

    Div header = new Div(
        drawerToggle,
        logo,
        applicationName,
        developerName
    );

    header.getStyle()
        .set("display", "flex")
        .set("align-items", "center")
        .set("gap", "12px")
        .set("width", "100%")
        .set("height", "64px")
        .set("padding", "0 24px 0 8px")
        .set("box-sizing", "border-box")
        .set("background", "#ffffff")
        .set("border-bottom", "1px solid " + BORDER_COLOR);

    return header;
  }

  /**
   * Creates the contents of the side navigation drawer.
   *
   * @return configured drawer component
   */
  private Component createDrawer() {

    Span navigationTitle = new Span("NAVIGATION");

    navigationTitle.getStyle()
        .set("font-size", "0.72rem")
        .set("font-weight", "700")
        .set("letter-spacing", "0.08em")
        .set("color", "#64748b");

    SideNav navigation = new SideNav();

    SideNavItem booksItem = new SideNavItem(
        "Books List",
        BooksView.class
    );

    navigation.addItem(booksItem);
    navigation.setWidthFull();

    Div drawer = new Div(
        navigationTitle,
        navigation
    );

    drawer.getStyle()
        .set("display", "flex")
        .set("flex-direction", "column")
        .set("gap", "14px")
        .set("padding", "24px 14px")
        .set("height", "100%")
        .set("box-sizing", "border-box")
        .set("background", "#f8fafc")
        .set("border-right", "1px solid " + BORDER_COLOR);

    // The accent line helps visually separate navigation from
    // the surrounding drawer content.
    navigation.getStyle()
        .set("border-left", "3px solid " + ACCENT_COLOR)
        .set("padding-left", "8px");

    return drawer;
  }
}