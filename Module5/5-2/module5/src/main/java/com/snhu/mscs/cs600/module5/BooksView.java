package com.snhu.mscs.cs600.module5;

import java.util.Arrays;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * BooksView -
 *
 * Purpose:
 *      Displays the available book data in a read-only grid.
 *
 * Implements:
 *      TestData - Provides the book records displayed by the view.
 *
 * Expected Results:
 *      The view displays book title, author, and publication year
 *      in a scrollable table.
 *
 * Version              Author                  Date            Description
 * ***********************************************************************
 *     1                 Christopher Bright      21-Oct-2026     Initial Creation
 *     2                 Christopher Bright      21-Oct-2026     Updated UI styling
 */
@Route(value = "books", layout = MainLayout.class)
@PageTitle("Books List")
public class BooksView extends VerticalLayout implements TestData {

  /**
   * Builds the book listing and configures the grid.
   */
  public BooksView() {

    setSizeFull();
    setPadding(false);
    setSpacing(false);

    getStyle()
        .set("background", "#f4f7f9")
        .set("padding", "32px");

    H2 viewTitle = new H2("Books List");

    viewTitle.getStyle()
        .set("margin", "0")
        .set("color", "#172235")
        .set("font-size", "1.75rem");

    Paragraph description = new Paragraph(
        "Browse the books currently available in the Module5 data set."
    );

    description.getStyle()
        .set("margin", "4px 0 22px 0")
        .set("color", "#64748b")
        .set("font-size", "0.95rem");

    /*
     * Automatic columns are disabled so the visible columns,
     * order, and labels stay under the control of this view.
     */
    Grid<Book> bookGrid = new Grid<>(Book.class, false);

    // Rows are display-only, so selection is not needed.
    bookGrid.setSelectionMode(Grid.SelectionMode.NONE);

    bookGrid.addColumn(Book::getTitle)
        .setHeader("Title")
        .setSortable(true)
        .setFlexGrow(2);

    bookGrid.addColumn(Book::getAuthor)
        .setHeader("Author")
        .setSortable(true)
        .setFlexGrow(1);

    bookGrid.addColumn(Book::getYear)
        .setHeader("Publication Year")
        .setSortable(true)
        .setWidth("170px")
        .setFlexGrow(0);

    // TestData already provides the records needed by this view.
    bookGrid.setItems(Arrays.asList(book_list));

    /*
     * A fixed height keeps large result sets inside the grid
     * instead of extending the entire page.
     */
    bookGrid.setWidthFull();
    bookGrid.setHeight("620px");

    bookGrid.getStyle()
        .set("border", "1px solid #d9e1e8")
        .set("border-radius", "10px")
        .set("overflow", "hidden")
        .set("background", "#ffffff");

    /*
     * Limit the content width so the table stays readable on
     * larger displays.
     */
    VerticalLayout content = new VerticalLayout(
        viewTitle,
        description,
        bookGrid
    );

    content.setPadding(false);
    content.setSpacing(false);
    content.setWidthFull();
    content.setMaxWidth("1400px");

    content.getStyle()
        .set("margin", "0 auto")
        .set("padding", "28px")
        .set("box-sizing", "border-box")
        .set("background", "#ffffff")
        .set("border", "1px solid #e2e8f0")
        .set("border-radius", "12px")
        .set("box-shadow", "0 2px 8px rgba(15, 23, 42, 0.06)");

    add(content);
  }
}