/*
 * EffectivePortfolioVersionMethod.java
 *
 * Created on 03.05.13 15:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.MMString;
import de.marketmaker.iview.pmxml.SimpleMM;

/**
* @author Markus Dick
*/
class EffectivePortfolioVersionMethod {
    private String pmDate;
    private MmTalkWrapper wrapper;

    public EffectivePortfolioVersionMethod(String pmDate, MmTalkWrapper wrapper) {
        this.pmDate = pmDate;
        this.wrapper = wrapper;
    }

    public void invoke() {
        final SimpleMM theDate;

        if(!StringUtil.hasText(this.pmDate)) {
            theDate = MmTalkHelper.nowAsDIDateTime();
            Firebug.log("Setting current date as effective date: " + ((MMDateTime) theDate).getValue());
        }
        else {
            if("null".equals(this.pmDate)) { //$NON-NLS$
                Firebug.log("Setting empty string (converts to delphi zero date) as effective date");

                //Steffen is checking how we can get round this crazy hack...
                //and also this most sophisticated Java null date emulating Delphi zero date stuff...

               /* final Date delphiMmTalkHackDate =
                        new Date(PmxmlConstants.ZERO_DATE.getTime() //The usual Delphi trash
                + 86400000); //The special MM-Talk hack
                pmDate = Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(delphiMmTalkHackDate);

                DIDateTime diDateTime = new DIDateTime();
                diDateTime.setValue(pmDate);
                theDate = diDateTime;*/

                MMString str = new MMString(); //Hack: PM (MM-Talk?!?) converts an empty String to a Delphi zero date.
                str.setValue("");
                theDate = str;
            }
            else {
                Firebug.log("Effective date: " + this.pmDate); //$NON-NLS$

                MMDateTime dateTime = new MMDateTime();
                dateTime.setValue(this.pmDate);
                theDate = dateTime;
            }
        }

        this.wrapper.updateParameter(Portfolio.EFFECTIVE_SINCE_PARAM, theDate);
    }
}
