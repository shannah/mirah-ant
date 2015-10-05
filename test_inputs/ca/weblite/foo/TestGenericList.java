package ca.weblite.foo;


import java.util.List;
/**
 *
 * @author shannah
 */
public class TestGenericList {

    /**
     *
     */
    public static final int ePREOP = 0;

    /**
     *
     */
    public static final int eC1 = 3;
// champ permettant de noter que le patient a été informé à propos de l'archivage informatique de ses données
    private Boolean infoPatient;

// Sync data
    private String syncState;

    /**
     *
     */
    public TestGenericList() {

    }

// ***************** Get + Set ***********************//
    /**
     *
     * @return
     */
    public Object getUser() {
        return null;
    }

    /**
     *
     * @param value
     */
    public void setUser(Object user) {
        
    }

    /**
     *
     * @return
     */
    public Integer getEnableInt() {
        return 0;
    }

// might not be used, but define a property
    /**
     *
     * @param i
     */
    public void setEnableInt(Integer i) {
        
    }

    /**
     *
     * @return
     */
    public Boolean getEnable() {
        return false;
    }

    /**
     *
     * @param value
     */
    public void setEnable(Boolean value) {
        
    }

    /**
     *
     * @return
     */
    public Long getNumImplant() {
        return 0l;
    }

    /**
     *
     * @param value
     */
    public void setNumImplant(Long value) {
        
    }

    /**
     *
     * @return
     */
    public Long getIdUser() {
        return 0l;
    }

    /**
     *
     * @param value
     */
    public void setIdUser(Long value) {
        
    }

    /**
     *
     * @return
     */
    public Long getIdEtatDossier() {
        return 0l;
    }

    
    /**
     *
     * @return
     */
    public List<TestGenericList> getListExamen() {
// on trie par ordre de numero d'examen

        /*
         * boolean bPermut = true; while (bPermut) { bPermut = false; for (int i
         * = 0; i < listExamen.size() - 1; i++) { Examen examen1 =
         * listExamen.get(i); Examen examen2 = listExamen.get(i + 1);
         * 
         * if (examen1.getNumero() > examen2.getNumero()) { listExamen.remove(i
         * + 1); listExamen.add(i, examen2); bPermut = true; } } }
         */
        return null;
    }

    /**
     *
     * @param value
     */
    public void setListExamen(List<Object> value) {
        
    }

    /**
     *
     * @return
     */
    public Object[] getEtape() {
        return null;
    }

}
