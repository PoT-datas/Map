package api.pot.map.tools;

public class GeoPos {
    public static double[] getPosFromDMS(String dms) {
        if(dms==null) return new double[]{91,181};
        dms.replaceAll(" ", "");
        try {
            int i, j, k;
            double d1 = Double.parseDouble(dms.substring(0, i=dms.indexOf("°"))),
                    m1 = Double.parseDouble(dms.substring(i+1, j=dms.indexOf("'")));
            double s1 = Double.parseDouble(dms.substring(j+1, k=dms.indexOf("\"")));
            int sg1 = dms.substring(k+1, k+2).toLowerCase().equals("s")?-1:1;
            dms = dms.substring(k+2);
            double d2 = Double.parseDouble(dms.substring(0, i=dms.indexOf("°"))),
                m2 = Double.parseDouble(dms.substring(i+1, j=dms.indexOf("'")));
            double s2 = Double.parseDouble(dms.substring(j+1, k=dms.indexOf("\"")));
            int sg2 = dms.substring(k+1, k+2).toLowerCase().equals("w")?-1:1;
            return new double[]{sg1*(d1+(m1/60d)+(s1/3600d)), sg2*(d2+(m2/60d)+(s2/3600d))};
        }catch (Exception e){return new double[]{91,-181};}
    }

    public static double[] getPosFromDDM(String ddm) {
        if(ddm==null) return new double[]{91,181};
        ddm.replaceAll("( )+", " ");
        try {
            int i, j;
            double d1 = Double.parseDouble(ddm.substring(0, i=ddm.indexOf(" "))),
                    m1 = Double.parseDouble(ddm.substring(i+1, j=ddm.indexOf(",")));
            int sg1 = (int) (d1/Math.abs(d1));
            d1 = Math.abs(d1);
            ddm = ddm.substring(j+2);
            double d2 = Double.parseDouble(ddm.substring(0, i=ddm.indexOf(" "))),
                    m2 = Double.parseDouble(ddm.substring(i+1));
            int sg2 = (int) (d2/Math.abs(d2));
            d2 = Math.abs(d2);
            return new double[]{sg1*(d1+(m1/60d)), sg2*(d2+(m2/60d))};
        }catch (Exception e){return new double[]{91,-181};}
    }

    public static double[] getPosFromDD(String dd) {
        if(dd==null) return new double[]{91,181};
        dd.replaceAll(" ", "");
        try {
            String str_lat = dd.substring(0, dd.indexOf(",")), str_lng = dd.substring(dd.indexOf(",")+1);
            return new double[]{Double.parseDouble(str_lat),Double.parseDouble(str_lng)};
        }catch (Exception e){return new double[]{91,181};}
    }
}
