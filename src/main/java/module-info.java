module ni.edu.uam.casos_de_estudio {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.casos_de_estudio to javafx.fxml;
    exports ni.edu.uam.casos_de_estudio;
}