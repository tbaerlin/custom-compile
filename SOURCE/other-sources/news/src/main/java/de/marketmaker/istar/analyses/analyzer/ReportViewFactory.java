package de.marketmaker.istar.analyses.analyzer;

import java.security.InvalidParameterException;
import java.util.Map.Entry;
import java.util.stream.Stream;

import de.marketmaker.istar.analyses.analyzer.ReportImpl.Builder;
import de.marketmaker.istar.analyses.analyzer.stream.Columns;
import de.marketmaker.istar.analyses.analyzer.stream.RowFunction;

/**
 * use a parser and builder to create a report view from a query then apply paging
 */
public class ReportViewFactory {

    private QueryParser parser;

    public boolean setup(String query) {
        parser = new QueryParser();
        return parser.parse(query);
    }

    public ReportView generateView(ReportContext reportContext) {
        assert reportContext != null;
        final ReportView view = new ReportView();
        view.setTitles(parser.getFields());

        final Builder builder = new Builder();
        builder.setSourceStream(parser.getStream());
        // the class for aggregating the requested data
        final Class valueClass = Columns.findClassForFields(parser.getFields());
        builder.setValueClass(valueClass);
        builder.setDate(parser.getDate());
        builder.setSortOrder(parser.getSort(), parser.getOrder());
        builder.setPriceCache(reportContext.getPriceCache());


        final ReportImpl delegate = builder.build();
        final Stream<Entry> stream = delegate.generate(reportContext);
        final RowFunction rowFunction = findFinisher(parser, valueClass);

        stream.skip(parser.getSkip())
                .limit(parser.getLimit())
                .forEach(entry -> view.addRow(rowFunction.apply(entry)));

        return view;
    }

    private RowFunction findFinisher(QueryParser parser, Class clazz) {
        RowFunction result;
        switch (parser.getStream()) {
            case "industry":
                result = new RowFunction(parser.getFields(), String.class, clazz);
                break;
            case "security":
                result = new RowFunction(parser.getFields(), Security.class, clazz);
                break;
            case "index":
                result = new RowFunction(parser.getFields(), Index.class, clazz);
                break;
            case "agency":
                result = new RowFunction(parser.getFields(), Agency.class, clazz);
                break;
            case "analysis":
                result = new RowFunction(parser.getFields(), Analysis.class, clazz);
                break;
            default:
                throw new InvalidParameterException("unknown finisher: " + parser.getStream() + " for class " + clazz);
        }
        return result;
    }

}
