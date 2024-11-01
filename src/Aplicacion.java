import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.jfree.ui.ApplicationFrame;

public class Aplicacion {

    private static List<Map<String, String>> data = new ArrayList<>();

    public static void main(String[] args) {
        // Usar JFileChooser para seleccionar el archivo CSV
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo CSV");
        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            cargarDatos(filePath);

            Scanner scanner = new Scanner(System.in);
            System.out.println("Seleccione filtros:");

            String ciudad = seleccionarFiltro("Ciudad");
            String genero = seleccionarFiltro("Género");
            String edad = seleccionarFiltro("Edad");
            String condicion = seleccionarFiltro("Condición Médica");

            List<Map<String, String>> datosFiltrados = filtrarDatos(ciudad, genero, edad, condicion);

            System.out.println("Top 3 de Ciudades con mayores registros:");
            Map<String, Long> top3Ciudades = obtenerTop3(datosFiltrados, "Ciudad");
            top3Ciudades.forEach((key, value) -> System.out.println(key + ": " + value));

            System.out.println("¿Desea visualizar los datos en un gráfico? (S/N): ");
            String respuestaGrafico = scanner.nextLine();
            if (respuestaGrafico.equalsIgnoreCase("S")) {
                System.out.println("Seleccione el tipo de gráfico (1 - Barras, 2 - Pastel): ");
                int tipoGrafico = scanner.nextInt();
                generarGrafico(top3Ciudades, tipoGrafico);
            }

            scanner.close();
        } else {
            System.out.println("No se seleccionó ningún archivo.");
        }
    }

    private static void cargarDatos(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = br.readLine().split(","); // Suponiendo que los encabezados están en la primera línea
            while ((line = br.readLine()) != null) {
                Map<String, String> row = new HashMap<>();
                String[] values = line.split(",");
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim()); // Trim para eliminar espacios en blanco
                }
                data.add(row);
            }
            System.out.println("Datos cargados correctamente.");
        } catch (IOException e) {
            System.out.println("Error al cargar los datos: " + e.getMessage());
        }
    }

    private static String seleccionarFiltro(String columna) {
        System.out.println("Valores disponibles para " + columna + ":");
        Set<String> valoresUnicos = data.stream()
                .map(row -> row.getOrDefault(columna, ""))
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());

        valoresUnicos.forEach(System.out::println);
        System.out.println("Ingrese un valor para " + columna + " (o presione Enter para omitir):");
        Scanner scanner = new Scanner(System.in);
        String seleccion = scanner.nextLine();
        return seleccion.isEmpty() ? "Todos" : seleccion;
    }

    private static List<Map<String, String>> filtrarDatos(String ciudad, String genero, String edad, String condicion) {
        return data.stream()
                .filter(row -> ciudad.equals("Todos") || ciudad.equals(row.get("Ciudad")))
                .filter(row -> genero.equals("Todos") || genero.equals(row.get("Género")))
                .filter(row -> edad.equals("Todos") || edad.equals(row.get("Edad")))
                .filter(row -> condicion.equals("Todos") || condicion.equals(row.get("Condición Médica")))
                .collect(Collectors.toList());
    }

    private static Map<String, Long> obtenerTop3(List<Map<String, String>> datos, String columna) {
        return datos.stream()
                .collect(Collectors.groupingBy(row -> row.getOrDefault(columna, ""), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private static void generarGrafico(Map<String, Long> datos, int tipoGrafico) {
        if (tipoGrafico == 1) {
            crearGraficoBarras(datos);
        } else if (tipoGrafico == 2) {
            crearGraficoPastel(datos);
        } else {
            System.out.println("Tipo de gráfico no válido.");
        }
    }

    private static void crearGraficoBarras(Map<String, Long> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        datos.forEach((key, value) -> dataset.addValue(value, "Cantidad", key));

        JFreeChart barChart = ChartFactory.createBarChart(
                "Top 3 de Ciudades",
                "Ciudad",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        mostrarGrafico(barChart);
    }

    private static void crearGraficoPastel(Map<String, Long> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        datos.forEach(dataset::setValue);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Top 3 de Ciudades",
                dataset,
                true, true, false);

        mostrarGrafico(pieChart);
    }

    private static void mostrarGrafico(JFreeChart chart) {
        ApplicationFrame frame = new ApplicationFrame("Visualización de Datos");
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        frame.setContentPane(chartPanel);
        frame.pack();
        UIUtils.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    private static class UIUtils {
        private static void centerFrameOnScreen(ApplicationFrame frame) {
            frame.setLocationRelativeTo(null); // Centrar la ventana en la pantalla
        }
    }
}

