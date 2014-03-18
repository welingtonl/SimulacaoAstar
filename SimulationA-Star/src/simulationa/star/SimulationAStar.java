package simulationa.star;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SimulationAStar extends JApplet {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("Simulation A*");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JApplet applet = new SimulationAStar();
        applet.init();
        frame.getContentPane().add(applet);
        frame.pack();
        frame.setLocationRelativeTo(null); //Centra aplicação no ecrã
        frame.setVisible(true);
        JOptionPane.showMessageDialog(applet, "Bem-vindo/a a aplicação de simulação do Algoritmo A*\nLeia a descrição abaxio da área de simulação para saber o que fazer", null, WIDTH);
    }

    public void init() {

        JPanel panel = new Panel();
        getContentPane().add(panel);
    }
}

class Panel extends JPanel implements Runnable, MouseListener, MouseMotionListener {
    int matrizArena[][] = new int[800][500];//[x][y] onde x=800 e y=500

    //Alvo(osso)   
    URL urlAlvo = getClass().getClassLoader().getResource("images/osso.png");
    int widthAlvo = 29;
    int heightAlvo = 29;
    float graus = 0;//Para a rotação do osso
    boolean rodaOsso = false;//Só quando o caminho estiver desenhado é que o osso roda    
    int xAlvo;
    int yAlvo;
    //Para caso o utilizador clicar fora da area simulada o rectangulo continuar a estar na posição anterior
    int xAlvoAntigo;
    int yAlvoAntigo;
    int xAlvoDepoisDeDesenhado;
    int yAlvoDepoisDeDesenhado;
    BufferedImage ImgAlvo;

    //CalculoAStar
    ArrayList xPosTodas = new ArrayList();//Serve para desenhar os quadrados que o CalculoAStar já analizou
    ArrayList yPosTodas = new ArrayList();//Serve para desenhar os quadrados que o CalculoAStar já analizou
    ArrayList xCaminhoObtido = new ArrayList();//Serve para desenhar o caminho obtido constantemente em Branco depois de o cão ter xegado ao alvo
    ArrayList yCaminhoObtido = new ArrayList();//Serve para desenhar o caminho obtido constantemente em Branco depois de o cão ter xegado ao alvo

    //Cão
    URL urlCao = getClass().getClassLoader().getResource("images/caoCara.png");
    int widthCao;
    int heightCao;
    int xCao = 100;
    int yCao = 100;
    int xCaoFinal = 0;
    int yCaoFinal = 0;
    int ponteiro = 0;//Este ponteiro serve para poder ir ao ArrayList de baixo e buscar os valores
    ArrayList AStar = new ArrayList(); //Posições diferentes do array. Aqui são armazenadas as posições do algoritmo
    ArrayList<Shape> shape = new ArrayList();//Este array server para desenhar o caminho encontrado pelo algoritmo, até ao alvo
    BufferedImage ImgCao;

    //Botão imprimir XY
    int xImprimir = 642;
    int yImprimir = 550;
    boolean impirmir = false;
    boolean verImprimir = false;
    ArrayList ConteudoImprimir = new ArrayList();

    //Botão ver simulação XY
    int xVerSimulação = 492;
    int yVerSimulação = 550;
    boolean verSimulacao = false;

    //Botão Parar Simulaçao XY
    int xPararSimulação = 492;
    int yPararSimulação = 550;
    boolean PararSimulacao = false;

    //Textura
    URL urlTextura = getClass().getClassLoader().getResource("images/textura.jpg");
    URL urlTextura1 = getClass().getClassLoader().getResource("images/textura1.jpg");
 
    //Coordenadas do rato para escrever a mensagem de erro
    int xRato;
    int yRato;

    boolean flag;//Caso a flag fique activa então é porque a volta do alvo existem paredes, ou está fora da area de simulação
    boolean startClique = false; //Primeiro clique do utilizador para não aparecer o alvo no arranque do jogo  

    //Mensagem a mostrar em caso de erro
    String message = "";
    String messagePercentagem = "Progresso em % : -";//Esta string é para mostrar a percentagem do calculo do A*
    String messageNosAnalizados = "Nos analisados    : -";
    String messageNosAteAlvo = "Nos ate ao alvo   : -";
    Font font;
    Color corTexto = Color.GRAY;

    //Efeito da descrição a chamar atenção
    Color corDesricao;
    int ms = 1000; //1000milisegundos=1segundo
    boolean troca; //Efeito de troca de cores
    int aFuncionar = 1;//Quando fica 0 então é porque o utilizador ja iniciou o algoritmo, ou seja, ja clicou numa area

    Thread calculo;//Thread para deixar o A* a funcionar em background
    boolean calculo_conluido = false;//Quando o A* estiver conluido então activa esta flag e é desenhado o precurso e o cao
    boolean calculo_em_andamento = false;//Isto para quando o A* estiver em funcionamento mostre o texto que está a calcular o precurso
    private BufferedImage ImgTextura;
    private BufferedImage ImgTextura1;

    ;

    public Panel() {
        try {
            this.ImgTextura1 = ImageIO.read(urlTextura1);
            this.ImgTextura = ImageIO.read(urlTextura);
            this.ImgAlvo = ImageIO.read(urlAlvo);
            this.ImgCao = ImageIO.read(urlCao);         
        } catch (IOException ex) {
            System.out.println("Line" + getLineNumber() + ": " + ex);
        }
        //Se o computador onde a aplicação é corrida não conseguir iniciar correctamente deve ser encerrada
        try {
            font = new Font("Verdana", Font.BOLD, 13);
            this.setPreferredSize(new Dimension(800, 600));
            this.setBackground(Color.black);
            Thread thread = new Thread(this);
            thread.start();
            addMouseListener(this);
            addMouseMotionListener(this);
        } catch (Exception ex) {
            System.out.println("Line" + getLineNumber() + ": " + ex);

        }
    }

    private int DesenhaParede(Graphics2D g2, int x, int y, int w, int h) {
        try {
            g2.setColor(Color.WHITE);
            g2.fillRect(x, y, 5, 5);
            matrizArena[x][y] = 1;  //Simboliza parede
            return 1;
        } catch (Exception ex) {
            System.out.println("Line" + getLineNumber() + ": " + ex);
            return 0;
        }
    }

    private int DesenhaArena(Graphics2D g2) {
        try {
            for (int y = 0; y < 500; y++) {
                for (int x = 0; x < 800; x++) {
                    //Quarto lado esquerdo  superior
                    if (y == 200 && x <= 200) {
                        DesenhaParede(g2, x, y, 1, 1);                             //Desenho X
                    } else if (x == 200 && (y >= 100 && y <= 200)) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Desenho Y
                    } else //Quarto lado direito inferior
                    if (y == 300 && x >= 600 && x <= 700) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Desenho X
                    } else if (x == 600 && y >= 300) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Desenho Y
                    } else //Quarto lado direito superior (duas entradas)
                    if (y == 200 && x >= 700) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Desenho X
                    } else if (x == 600 && y >= 70 && y <= 200) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Desenho Y
                    } else //Quarto esquerdo inferior
                    if (y == 350 && x <= 350) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Desenho X
                    } else if (y >= 350 && y <= 400 && x == 350) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Desenho Y
                    } else if (y >= 420 && x == 200) {                              //Quarto meio
                        DesenhaParede(g2, x, y, 1, 1);
                    } else //Quarto centro
                    if (y >= 70 && y <= 120 && x == 350) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Parede esquerda Y
                    } else if (y == 70 && x >= 350 && x <= 450) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Parede superior X
                    } else if (y >= 200 && y <= 250 && x == 350) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Parede esquerda inferior Y
                    } else if (y == 250 && x >= 350 && x <= 450) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Parede de baixo X
                    } else if (y >= 150 && y <= 180 && x == 450) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Parede de direita Y
                    } else if (x == 0 && y >= 0 && y <= 501) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Parede principal da esquerda
                    } else if (x == 795 && y >= 0 && y <= 501) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Parede principal da direita
                    } else if (y == 0 && x >= 0 && x <= 795) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Parede principal superior
                    } else if (y == 499 && x >= 0 && x <= 795) {
                        DesenhaParede(g2, x, y, 1, 1);                              //Parede principal inferior
                    }
                }
            }
            return 1;
        } catch (Exception ex) {
            System.out.println("Line" + getLineNumber() + ": " + ex);
            return 0;
        }
    }

    private int DesenhaImagem(Graphics2D g2, BufferedImage Img, int x, int y, int osso, int cao) {
        //Isto para poder obter o width e o heigth do objecto a ser desenhado
        int widthInterior = 0;
        int heigthInterior = 0;

        if (osso == 1) { //Se 1 é porque o metodo foi chamado para desenhar o osso
            widthAlvo = ImgAlvo.getWidth() - 1;
            heightAlvo = ImgAlvo.getHeight() - 1;
            widthInterior = widthAlvo;
            heigthInterior = heightAlvo;

            //Coloca na matrizArena a posição do alvo (onde simboliza uma parede para os leos)
            for (int i = xAlvoAntigo - (widthAlvo / 2); i <= xAlvoAntigo + (widthAlvo / 2); i++) {
                for (int j = yAlvoAntigo - (heightAlvo / 2); j <= yAlvoAntigo + (heightAlvo / 2); j++) {
                    matrizArena[i][j] = 2;
                }
            }
            if (rodaOsso == true) {//So quando o calculo estiver em andamento é que roda
                //TRANSLAÇÃO DO OSSO (OSSO RODA)
                g2.translate(x, y);
                g2.rotate(graus);
                g2.drawImage(ImgAlvo, -ImgAlvo.getWidth() / 2, -ImgAlvo.getHeight() / 2, null);

                g2.rotate(-graus);
                graus += 0.1;
                if (graus >= 360) {
                    graus = 0;
                }
                g2.translate(-x, -y);//Volta ao mesmo sitio
            } else {//Se o calculo não estiver em andamento então desenha normal
                AffineTransform at = new AffineTransform(g2.getTransform());
                x -= (widthInterior / 2);
                y -= (heigthInterior / 2);
                if (x > 0 && x < (800 - widthInterior) && y > 0 && y < (600 - heigthInterior)) {//Isto para evitar o desenho fora da area de simulação
                    at.translate(x, y);
                    g2.drawImage(ImgAlvo, at, this);
                }
            }
            return 1;
            //FIM TRANSLAÇÃO DO OSSO (OSSO RODA)
        } else if (cao == 1) {//Se 1 é porque o metodo foi chamado para desenhar o cao
            widthCao = ImgCao.getWidth() - 1;
            heightCao = ImgCao.getHeight() - 1;
            widthInterior = widthCao;
            heigthInterior = heightCao;

            AffineTransform at = new AffineTransform(g2.getTransform());
            x -= (widthInterior / 2);
            y -= (heigthInterior / 2);
            if (x > 0 && x < (800 - widthInterior) && y > 0 && y < (600 - heigthInterior)) {//Isto para evitar o desenho fora da area de simulação
                at.translate(x, y);
                g2.drawImage(ImgCao, at, this);
                return 1;
            } else {
                return 0;//Se nada for desenhado entao devolve 0
            }
        } else {
            return 0;//Se nada for desenhado entao devolve 0
        }
    }

    private int DesenhaAlvo(Graphics2D g2) {
        try {
            //Verificar se a volta do desenho do alvo existem paredes
            int aux1 = xAlvo - (widthAlvo / 2);
            int aux2 = yAlvo - (heightAlvo / 2);

            //500 e 800 é a area total da simulação
            int aux3 = 800 - widthAlvo;
            int aux4 = 500 - heightAlvo;

            if (xAlvo >= 0
                    && yAlvo >= 0
                    && aux1 > 0
                    && aux2 > 0
                    && aux3 > xAlvo
                    && aux4 > yAlvo) {

                //xAlvo e yAlvo são as posições do rato quando o utilizador clicou
                for (int x = xAlvo - (widthAlvo / 2) - 17; x <= xAlvo + (widthAlvo / 2) + 17; x++) { //widthAlvo/2 para poder centrar o alvo no rectangulo
                    if (x < 0) {
                        flag = true;
                        break;
                    }
                    for (int y = yAlvo - (heightAlvo / 2) - 17; y <= yAlvo + (heightAlvo / 2) + 17; y++) { //heightAlvo/2 para poder centrar o alvo no rectangulo                                                           
                        if (y < 0) {
                            flag = true;
                            break;
                        }
                        if (matrizArena[x][y] == 1 && x < 800 & y < 500) { //Parede encontrada                            
                            flag = true;
                            break;
                        }
                    }
                    //Se parede encontrada então o segundo ciclo também é forçado a parar
                    if (flag) {
                        break;
                    }
                }
            } else {
                flag = true; //Se chegar aqui é porque o utilizador clicou fora da aréa simulada com certeza
            }

            //Se a volta do desenho do alvo não houver paredes e o clique do utilizador foi dentro da aréa de simulação
            if (!flag) {
                //Posição do alvo actualizada
                startClique = true;//Primeiro clique significa, inicio de jogo. Então são guardadas as coordenadas
                aFuncionar = 0; //Efeito do texto da descrição é parada

                //Retira da matrizArena a posição do alvo (onde simboliza uma parede para os leos)
                if (xAlvoAntigo >= widthAlvo && yAlvoAntigo >= heightAlvo) {//Iso para por exemolo no primeiro clique quando o xAlvoaAtigo e yAlvoAntigo são nulos
                    for (int x = xAlvoAntigo - (widthAlvo / 2); x <= xAlvoAntigo + (widthAlvo / 2); x++) {
                        for (int y = yAlvoAntigo - (heightAlvo / 2); y <= yAlvoAntigo + (heightAlvo / 2); y++) {
                            matrizArena[x][y] = 0;
                        }
                    }
                }

                //Actualiza a posição do alvo
                xAlvoAntigo = xAlvo;
                yAlvoAntigo = yAlvo;
                DesenhaImagem(g2, ImgAlvo, xAlvo, yAlvo, 1, 0);
            }

            //Desenhar alvo com as ultimas coordenadas correctas caso tenha parede a volta do novo clique ou o mesmo foi fora da area de simulação
            if (flag && startClique == true) {
                DesenhaImagem(g2, ImgAlvo, xAlvoAntigo, yAlvoAntigo, 1, 0);
            }

            return 1;
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (xAlvoAntigo > 0 && yAlvoAntigo > 0) {//Isto para não provocar o piscar do alvo
                DesenhaImagem(g2, ImgAlvo, xAlvoAntigo, yAlvoAntigo, 1, 0);
            }
            System.out.println("Line" + getLineNumber() + ": " + ex);
            return 0;
        }
    }

    private int DesenhaBotoes(Graphics2D g2) {
        try {
            //Desenhar botões: Ver simulação | ver proxima iteração 
            if (aFuncionar == 0 && calculo_em_andamento == false) {
                g2.setFont(font);
                //g2.setColor(corDesricao);
                //Color aux = g2.getColor();//Backup da cor ja utilizada
                if ((xRato >= xVerSimulação && xRato <= xVerSimulação + 130)//Este if é para pintar o botão assim que o rato estiver em cima
                        && (yRato >= yVerSimulação && yRato <= yVerSimulação + 40)) {
                    
                    //Sombra transparente
                    GradientPaint BlackToWhite = new GradientPaint(500, 552, new Color(255, 255, 255, 0), 600, 572, new Color(0, 0, 0, 200));
                    g2.setPaint(BlackToWhite);
                    g2.fill(new Ellipse2D.Double(xVerSimulação + 3, yVerSimulação - 6, 130, 32));

                    //Botao com gradiente
                    BlackToWhite = new GradientPaint(500, 552, new Color(255, 255, 255, 0), 600, 572, new Color(34, 139, 34));
                    g2.setPaint(BlackToWhite);
                    g2.fill(new Ellipse2D.Double(xVerSimulação, yVerSimulação, 130, 40));//Ellipse a volta do texto para simular botão                                    
                    g2.setColor(Color.WHITE);
                    g2.drawString("Simular", 532, 575);
                } else {
                    g2.setColor(new Color(100, 250, 100, 100));
                    g2.fillRect(xVerSimulação, yVerSimulação, 130, 40);//Rectangulo a volta do texto para simular botão            
                    g2.setColor(Color.BLACK);
                    g2.drawString("Ver simulação", 505, 575);

                }
                if (verImprimir == true) {
                    if ((xRato >= xImprimir && xRato <= xImprimir + 115)//Este if é para pintar o botão assim que o rato estiver em cima
                            && (yRato >= yImprimir && yRato <= yImprimir + 40)) {
                        //Sombra botão
                        GradientPaint BlackToWhite = new GradientPaint(650, 552, new Color(255, 255, 255, 0), 750, 572, new Color(0, 0, 0, 200));
                        g2.setPaint(BlackToWhite);
                        g2.fill(new Ellipse2D.Double(xImprimir + 3, yImprimir - 6, 115, 32));

                        //botão
                        BlackToWhite = new GradientPaint(650, 552, new Color(255, 255, 255, 0), 750, 572, new Color(0, 0, 255));
                        g2.setPaint(BlackToWhite);
                        g2.fillOval(xImprimir, yImprimir, 115, 40);//Elipse a volta do texto para simular botão 
                        g2.setColor(Color.WHITE);
                        g2.drawString("Guardar", 668, 575);
                    } else {
                        g2.setColor(new Color(100, 100, 250, 100));
                        g2.fillRect(xImprimir, yImprimir, 115, 40);//Rectangulo a volta do texto para simular botão  
                        g2.setColor(Color.black);
                        g2.drawString("Guardar", 668, 575);
                    }
                }
            } else if (calculo_em_andamento == true) {
                if ((xRato >= xPararSimulação && xRato <= xPararSimulação + 130)//Este if é para pintar o botão assim que o rato estiver em cima
                        && (yRato >= yPararSimulação && yRato <= yPararSimulação + 40 && PararSimulacao == false)) {

                    //Sombra botão
                    GradientPaint BlackToWhite = new GradientPaint(500, 552, new Color(255, 255, 255, 0), 600, 572, new Color(0, 0, 0, 200));
                    g2.setPaint(BlackToWhite);
                    g2.fill(new Ellipse2D.Double(xPararSimulação + 3, yPararSimulação - 6, 130, 32));

                    //Botão
                    BlackToWhite = new GradientPaint(500, 552, new Color(255, 255, 255, 0), 600, 572, Color.RED);
                    g2.setPaint(BlackToWhite);
                    g2.fillOval(xPararSimulação, yPararSimulação, 130, 40);//Elipse a volta do texto para simular botão 
                    g2.setColor(Color.WHITE);
                    g2.drawString("Parar", 536, 575);
                } else if (PararSimulacao == false) {
                    g2.setColor(new Color(250, 100, 100, 100));
                    g2.fillRect(xVerSimulação, yVerSimulação, 130, 40);//Rectangulo a volta do texto para simular botão            
                    g2.setColor(Color.BLACK);
                    g2.drawString("Parar", 536, 575);

                }
            }
            if (aFuncionar == 1 && calculo_em_andamento == false) {
                //Descriçaõ
                g2.setFont(font);
                g2.setColor(corDesricao);
                g2.drawString("Clique numa área para posicionar o alvo", 16, 557);
            }
            return 1;
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Line" + getLineNumber() + ": " + ex);
            return 0;
        }
    }

    private int BotaoImprimirPremido() {
        JFileChooser fc = new JFileChooser();

        fc.setDialogTitle("Guardar todos os calculos do ultimo percruso");
        fc.setSelectedFile(new File("CalculosPercurso.CSV"));
        int resposta = fc.showSaveDialog(Panel.this);
        if (resposta == JFileChooser.APPROVE_OPTION) {
            try {
                File file = new File(fc.getCurrentDirectory().toString() + "\\" + fc.getSelectedFile().getName().toString());
                // Se não existe o ficheiro é criado
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw;
                fw = new FileWriter(file.getAbsoluteFile());

                BufferedWriter bw = new BufferedWriter(fw);
                for (int i = 0; i < ConteudoImprimir.size(); i++) {
                    bw.write(ConteudoImprimir.get(i).toString());
                }
                bw.close();
                String textoMessage = "Ficheiro guardado com sucesso.\nPertende abrir o ficheiro?\n\nNOTA: Apesar do ficheiro estar com a extensão .CSV para uma\nmelhor análise é recomendado abrir com o Excel";
                String titulo = "Sucesso";
                resposta = JOptionPane.showConfirmDialog(null, textoMessage, titulo, JOptionPane.YES_NO_OPTION);
                if (resposta == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(new File(fc.getCurrentDirectory().toString() + "\\" + fc.getSelectedFile().getName().toString()));
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                System.out.println("Line" + getLineNumber() + ": " + ex);
            }
        }
        return 1;
    }

    private int BotaoVerSimulacaoPremido(Graphics2D g2) {
        try {
            if (verSimulacao == true && calculo_conluido == false && calculo_em_andamento == false) {
                DesenhaImagem(g2, ImgCao, xCao, yCao, 0, 1);//O cão aqui encontra-se sempre na mesma posiçao (Isto para não piscar quando e clicado no botao de ver a simulação)
                ponteiro = 0;//O ponteiro é colocado a 0 para redesenhar assim que o Calculo_AStar() estiver concluido
                shape = new ArrayList();//O array com o caminho ate ao alvo é limpo
                if (calculo_em_andamento == false) { //Se o calculo nao estiver concluido inicia se uma thread para ir calculando as posições
                    calculo_em_andamento = true;
                    message = "A procurar o caminho mais perto... aguarde";
                    messagePercentagem = "Progresso em % : -";//Esta string é para mostrar a percentagem do calculo do A*
                    messageNosAnalizados = "Nos analisados    : -";
                    messageNosAteAlvo = "Nos ate ao alvo   : -";
                    rodaOsso = false;//Uma vez  que o cao chegou ao osso, a rotação do osso parou
                    calculo = new Thread() {
                        @Override
                        public void run() {
                            calculo_em_andamento = true;//Activa a flag para poder tirar os botoes e colocar uma string como aviso
                            AStar = Calculo_AStar();
                        }
                    };
                    calculo.setPriority(Thread.MIN_PRIORITY);
                    calculo.start();//Thread é iniciada                   
                }
            } else if (verSimulacao == false || calculo_conluido == false) {//calculo_conluido=false, então significa que o A* não está em funcionamento
                DesenhaImagem(g2, ImgCao, xCao, yCao, 0, 1);//O cão aqui encontra-se sempre na mesma posiçao
            } else if (calculo_conluido == true && ponteiro >= 0) {   //Quando o calculo das posições do desenho estiver concluido                                       
                message = "A caminhar até ao alvo...";//Limpa a mensagem de texto                               
                //Cão
                String aux = AStar.get(ponteiro).toString();
                int lenth, pontos = 0;
                for (lenth = 0; lenth <= aux.length(); lenth++) {
                    if (":".equals(aux.substring(lenth, lenth + 1))) {
                        pontos = lenth;//Obtenção dos dois pontos para a separação de X com Y
                        break;
                    }
                }
                this.xCao = Integer.parseInt(aux.substring(0, pontos));//Obtenção da posição X
                this.yCao = Integer.parseInt(aux.substring(pontos + 1, aux.length()));//Obtenção da posição Y               
                //Fim cão

                //Desenho do precurso
                for (int i = 0; i < AStar.size(); i++) {
                    aux = AStar.get(i).toString();
                    for (lenth = 0; lenth <= aux.length(); lenth++) {
                        if (":".equals(aux.substring(lenth, lenth + 1))) {
                            pontos = lenth;
                            break;
                        }
                    }
                    shape.add(new Rectangle2D.Double(Integer.parseInt(aux.substring(0, pontos)), Integer.parseInt(aux.substring(pontos + 1, aux.length())), 2, 3));
                    g2.setColor(Color.green);
                    g2.fill(shape.get(shape.size() - 1));
                }
                DesenhaImagem(g2, ImgCao, xCao, yCao, 0, 1);//Desenho da imagem 
                //Fim desenho do precurso

                AStar.remove(ponteiro);//Remove o objecto desenhado para depois não aparecer novamente o caminho onde o cao ja foi desenhado
                ponteiro -= 1;//incremetnação do ponteiro para a obtenção da proxima posição: Maior diferença mais velocidade no desenho do cão
            } else if (ponteiro < 0) {//É quando o caminho e o osso já foram desenhados, então faz um reset as variaveis               
                DesenhaImagem(g2, ImgCao, xCao, yCao, 0, 1);//Isto para não piscar o desenho do cão quando o mesmo chegou ao alvo                                               
                message = "";
                xCao = xCaoFinal;
                yCao = yCaoFinal;
                verSimulacao = false; //Botão "Ver simulação" passa a não estar premido
                calculo_em_andamento = false;
                calculo_conluido = false; //calculo_conluido=false, então significa que o A* não está em funcionamento                                
                calculo.stop();
            }
            return 1;
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Line" + getLineNumber() + ": " + ex);
            return 0;
        }
    }

    private ArrayList Calculo_AStar() {
        ConteudoImprimir = new ArrayList();
        xPosTodas = new ArrayList();
        yPosTodas = new ArrayList();
        xCaminhoObtido = new ArrayList();
        yCaminhoObtido = new ArrayList();
        ConteudoImprimir.add(";;;;;;Posicoes X e Y\n;;;;;;============\n;;;;;;xOsso=" + xAlvoAntigo + "\n;;;;;;yOsso=" + yAlvoAntigo + "\n\n;;;;;;xCao=" + xCao + "\n;;;;;;yCao=" + yCao + "\n\n\n\nxCao e yCao;Custo G;Custo H;F=G+H;Pai: xCao e yCao;Alvo atingido(True)\n==========;=======;=======;=======;============;================\n");
        ArrayList Retorno = new ArrayList();//Array com todas as posições até ao alvo       
        ArrayList listaAbertaX = new ArrayList(); //Posições a serem analizadas do eixo X
        ArrayList listaAbertaY = new ArrayList(); //Posições a serem analizadas do eixo Y       
        int ultimoListaAbertaX = 0;
        int ultimoListaAbertaY;

        int ultimoListaFechadaX;
        int ultimoListaFechadaY;

        ArrayList listaFechadaX = new ArrayList(); //Posições finais ate ao alvo do eixo x
        ArrayList listaFechadaY = new ArrayList(); //Posições finais ate ao alvo do eixo Y

        ArrayList paiQuadradoX = new ArrayList();//Posições X com o caminho correcto 
        ArrayList paiQuadradoY = new ArrayList();//Posições Y com o caminho correcto 

        ArrayList G = new ArrayList(); //INTEGER é o custo do movimento para se mover do ponto de início até o quadrado determinado(10 para vertical e horizonatal e 14 par diagonal)  
        int ultimoG;        

        int hInicial = Integer.MAX_VALUE; //Isto para poder calcular a percentagem do percurso
        int diferenca_hInicial_ultimoH = Integer.MAX_VALUE;//Isto para poder verificar se o ultimo H é inferior ao h obtido anteriormente        

        ArrayList H = new ArrayList(); //DOUBLE é o custo estimado do movimento para mover daquele quadrado determinado até o destino final (Neste caso é a hipotenusa)
        double ultimoH = 0;

        ArrayList F = new ArrayList(); //DOUBLE é o somatório de G com H 
        double ultimoF;

        int xPos[] = new int[8];//Array com o valor das posições do eixo XX
        int yPos[] = new int[8];//Array com o valor das posições do eixo YY

        double catetoAdj;
        double catetoOpo;
        double hipotenusa;

        int peso;
        int distanciaPrecorrida = 0;
        int posicaoPaiFinal = 0;
        listaAbertaX.add(this.xCao);
        listaAbertaY.add(this.yCao);
        G.add(0);
        H.add(0);
        F.add(0);
              
        do {
            if (PararSimulacao == true) {//Quando o botão "Parar" for premido esta flag é activa, e portanto o processo de calulo é interrompido
                break;
            }
            distanciaPrecorrida++;//Para poder obter o custo do G conforme o algoritmo vai afastando
            double custoMenor = Double.POSITIVE_INFINITY;
            int xyPos = 0;
            double custo;
            for (int i = 0; i < listaAbertaX.size(); i++) {
                custo = Double.parseDouble(F.get(i).toString());
                if (custo <= custoMenor) {
                    custoMenor = custo;
                    xyPos = i;
                }
            }

            //Quadrado pai
            listaFechadaX.add(listaAbertaX.get(xyPos));
            listaFechadaY.add(listaAbertaY.get(xyPos));
            ultimoListaFechadaX = Integer.parseInt(listaFechadaX.get(listaFechadaX.size() - 1).toString());
            ultimoListaFechadaY = Integer.parseInt(listaFechadaY.get(listaFechadaY.size() - 1).toString());

            //O escolhido foi removido da lista aberta e do F
            listaAbertaX.remove(xyPos);
            listaAbertaY.remove(xyPos);
            F.remove(xyPos);

            if (ultimoListaFechadaX >= xAlvoAntigo - (widthAlvo / 11) && ultimoListaFechadaX <= xAlvoAntigo + (widthAlvo / 11) //widthAlvo+10 e widthAlvo-10 são apenas ajustes para se poder centrar no alvo no eixo dos XX
                    && ultimoListaFechadaY >= yAlvoAntigo - (heightAlvo / 11) && ultimoListaFechadaY <= yAlvoAntigo + (heightAlvo / 11)) {//heightAlvo+10 e heightAlvo-10 são apenas ajustes para se poder centrar no alvo no eixo dos YY
                for (int i = 0; i < xPosTodas.size(); i++) {
                    if (Integer.parseInt(xPosTodas.get(i).toString()) == ultimoListaFechadaX
                            && Integer.parseInt(yPosTodas.get(i).toString()) == ultimoListaFechadaY) {
                        posicaoPaiFinal = i;
                        xAlvoDepoisDeDesenhado = xAlvoAntigo;//Isto para quando o utilizador clicar para calcular o caminho, caso o cão esteja 
                        yAlvoDepoisDeDesenhado = yAlvoAntigo;
                        break;//Pra sair do for
                    }
                }
                break;//Para sair do While principal
            }

            peso = 5;
            int x = ultimoListaFechadaX;
            int y = ultimoListaFechadaY;
            if (x < peso) {
                x = peso;
            } else if (x > 800) {
                x = 800 - widthCao;
            }
            if (y < peso) {
                y = peso;
            } else if (y > 500) {
                y = 500 - heightCao;
            }

            xPos[0] = x + peso;
            yPos[0] = y;

            xPos[1] = x + peso;
            yPos[1] = y + peso;

            xPos[2] = x;
            yPos[2] = y + peso;

            xPos[3] = x - peso;
            yPos[3] = y + peso;

            xPos[4] = x - peso;
            yPos[4] = y;

            xPos[5] = x - peso;
            yPos[5] = y - peso;

            xPos[6] = x;
            yPos[6] = y - peso;

            xPos[7] = x + peso;
            yPos[7] = y - peso;

            for (int quadrado = 0; quadrado < 8; quadrado++) {//Para cada um dos qudrados a volta do quadrado pai
                //  Se estiver na lista fechada
                boolean existeListaFechada = false;
                for (int j = 0; j < listaFechadaX.size(); j++) {
                    if (Integer.parseInt(listaFechadaX.get(j).toString()) == xPos[quadrado]//ListaFechadaX
                            && Integer.parseInt(listaFechadaY.get(j).toString()) == yPos[quadrado]) {//ListaFechadaY
                        existeListaFechada = true;
                        break;
                    }
                }

                //Vamos ver se existe parede a volta
                boolean paredeEncontrada = false;
                if (existeListaFechada == false) {
                    try {
                        for (int i = xPos[quadrado] - 5; i < (xPos[quadrado] + 10); i++) {
                            for (int j = yPos[quadrado] - 5; j < (yPos[quadrado] + (heightCao / 2) - 5); j++) {//                               
                                if (matrizArena[i][j] == 1) {
                                    paredeEncontrada = true;
                                }
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Line: " + getLineNumber() + " " + ex);
                    }
                }

                if (existeListaFechada == false && paredeEncontrada == false) {//Se o quadrado a analizar não está na lista fechada nem tem parede a volta 
                    //Se estiver na lista aberta
                    boolean existeListaAberta = false;
                    for (int j = 0; j < listaAbertaX.size(); j++) {
                        if (Integer.parseInt(listaAbertaX.get(j).toString()) == xPos[quadrado]//ListaFechadaX
                                && Integer.parseInt(listaAbertaY.get(j).toString()) == yPos[quadrado]) {//ListaFechadaY
                            existeListaAberta = true;                           
                            break;
                        }
                    }

                    if (existeListaAberta == false) {
                        listaAbertaX.add(xPos[quadrado]);
                        listaAbertaY.add(yPos[quadrado]);
                        ultimoListaAbertaX = Integer.parseInt(listaAbertaX.get(listaAbertaX.size() - 1).toString());
                        ultimoListaAbertaY = Integer.parseInt(listaAbertaY.get(listaAbertaY.size() - 1).toString());

                        paiQuadradoX.add(ultimoListaFechadaX);
                        paiQuadradoY.add(ultimoListaFechadaY);

                        if (quadrado == 0 || quadrado == 2 || quadrado == 4 || quadrado == 6) {//Guarda o G referente ao quadrado
                            G.add(distanciaPrecorrida * 10);
                        } else {
                            G.add((distanciaPrecorrida * 10) + 4);
                        }
                        ultimoG = Integer.parseInt(G.get(G.size() - 1).toString());

                        catetoAdj = Math.pow(Math.abs(xAlvoAntigo - ultimoListaAbertaX), 2);//Este cateto fica logo ao quadrado
                        catetoOpo = Math.pow(Math.abs(yAlvoAntigo - ultimoListaAbertaY), 2);//Este cateto fica logo ao quadrado
                        hipotenusa = Math.sqrt(catetoAdj + catetoOpo);

                        H.add(hipotenusa);
                        ultimoH = Double.parseDouble(H.get(H.size() - 1).toString());

                        if (hInicial == Integer.MAX_VALUE) {
                            hInicial = (int) ultimoH;//Simboliza o primeiro h obtido
                        } else {
                            if (ultimoH < hInicial && ultimoH < diferenca_hInicial_ultimoH) {
                                diferenca_hInicial_ultimoH = (int) ultimoH;
                                messagePercentagem = "Progresso em % : " + (Math.round(((hInicial - ultimoH) * 100) / hInicial)) + "%";
                            }
                        }

                        F.add(ultimoG + ultimoH);
                        ultimoF = Double.parseDouble(F.get(F.size() - 1).toString());

                        xPosTodas.add(xPos[quadrado]);
                        yPosTodas.add(yPos[quadrado]);
                    //    System.out.println(xPos[quadrado] + " - " + yPos[quadrado] + ";" + (Math.round(ultimoG * 1000) / 1000) + ";" + (Math.round(ultimoH * 1000) / 1000) + ";" + (Math.round(ultimoF * 1000) / 1000) + ";" + paiQuadradoX.get(paiQuadradoX.size() - 1).toString() + " - " + paiQuadradoY.get(paiQuadradoY.size() - 1).toString());
                        ConteudoImprimir.add(xPos[quadrado] + " e " + yPos[quadrado] + ";" + (Math.round(ultimoG * 1000) / 1000) + ";" + (Math.round(ultimoH * 1000) / 1000) + ";" + (Math.round(ultimoF * 1000) / 1000) + ";" + paiQuadradoX.get(paiQuadradoX.size() - 1).toString() + " e " + paiQuadradoY.get(paiQuadradoY.size() - 1).toString() + "\n");
                        messageNosAnalizados = "Nos analisados    : " + ConteudoImprimir.size();
                    }//Fim do if(não está na lista aberta)                   
                }//Fim do if(não existe na lista fechada e parede não encontrada)

            }//Fim do for principal            
        } while (!listaAbertaX.isEmpty()//O ciclo sera encerrado quando a lista aberta estiver vazia. Isto significa que não existe caminho possivel
                || !listaAbertaY.isEmpty());

        if (!listaAbertaX.isEmpty() && !listaAbertaY.isEmpty() && PararSimulacao == false) {
            //Buscar todas as posições ate ao alvo, utilizando o PAI de cada quadrado
            int paiX = 0;
            int paiY = 0;
            int auxX, auxY;
            int j = posicaoPaiFinal;
            for (int i = j; i > 0; i--) {//Vou buscar o pai da  posição
                paiX = Integer.parseInt(paiQuadradoX.get(i).toString());
                paiY = Integer.parseInt(paiQuadradoY.get(i).toString());
                for (j = 0; j < xPosTodas.size(); j++) {//Vou guardar o indice da posição cujo pai é igual
                    auxX = Integer.parseInt(xPosTodas.get(j).toString());
                    auxY = Integer.parseInt(yPosTodas.get(j).toString());
                    if (auxX == paiX && auxY == paiY) {
                        Retorno.add(auxX + ":" + auxY);
                        xCaminhoObtido.add(paiX);
                        yCaminhoObtido.add(paiY);
                        i = j + 1;//Agora vou buscar o pai cuja posição passou a ser a ultima
                        break;
                    }
                }
            }
            //Ultima posição do cão fixa
            xCaoFinal = Integer.parseInt(xPosTodas.get(posicaoPaiFinal).toString());
            yCaoFinal = Integer.parseInt(yPosTodas.get(posicaoPaiFinal).toString());
            String objecto = ConteudoImprimir.get(posicaoPaiFinal).toString();

            objecto = objecto.substring(0, objecto.length() - 1);
            objecto += ";===TRUE===\n";

            ConteudoImprimir.set(posicaoPaiFinal, objecto);
            //O ponteiro para desenhar o caminho e o andamento do cao toma o valor do Array uma vez que vai andar de tras para frente
            ponteiro = Retorno.size() - 1;
            calculo_conluido = true;
            verImprimir = true;//Botao imprimir visivel 
            messagePercentagem = "Progresso em % : 100%";
            messageNosAteAlvo = "Nos ate ao alvo   :  " + Retorno.size();
            PararSimulacao = false;
            return Retorno;
        } else {//Se o programa chegar aqui é porque não existe caminho possivel ate ao alvo
            Retorno = new ArrayList();//Limpa o arrei que pode ter posições calculadas
            try {
                Retorno.add(listaFechadaX.get(0).toString() + ":" + listaFechadaY.get(0).toString());//Posição do cao antes de entrar no calclulo
                xCaoFinal = Integer.parseInt(listaFechadaX.get(0).toString());//Posição do cao antes de entrar no calclulo
                yCaoFinal = Integer.parseInt(listaFechadaY.get(0).toString());//Posição do cao antes de entrar no calclulo
                messagePercentagem = "Progresso em % : -";
                messageNosAteAlvo = "Nos ate ao alvo   : -";
                ponteiro = Retorno.size() - 1;
                PararSimulacao = false;
                calculo_conluido = true;
            } catch (IndexOutOfBoundsException ex) {
                System.out.println("Line" + getLineNumber() + ": " + ex);
            }
            return Retorno;
        }
    }

    private void DesenharString(Graphics2D g2, String mensagem) {
        if (startClique == true) {
            g2.setColor(corTexto);
            g2.setFont(font);
            g2.drawString(mensagem, 16, 530);
            g2.drawString(messagePercentagem, 16, 557);
            g2.drawString(messageNosAteAlvo, 16, 592);
            g2.drawString(messageNosAnalizados, 16, 575);
        }
    }

    private int AnalisePosicoes(Graphics2D g2) {
        if (xPosTodas.size() - 1 > 0) {
            try {
                g2.setColor(new Color(100, 100, 250, 100));
                for (int i = 0; i < xPosTodas.size(); i++) {
                    g2.drawRect(Integer.parseInt(xPosTodas.get(i).toString()), Integer.parseInt(yPosTodas.get(i).toString()), 5, 5);
                }
            } catch (java.lang.NullPointerException ex) {
                System.out.println("Line" + getLineNumber() + ": " + ex);
            }
        }
        return 1;
    }

    private int DesenharCaminhoObtido(Graphics2D g2) {
        if (xCaminhoObtido.size() - 1 > 0) {
            try {
                g2.setColor(Color.WHITE);
                if (ponteiro < 0) {
                    for (int i = xCaminhoObtido.size() - 1; i >= 0; i--) {
                        g2.drawRect(Integer.parseInt(xCaminhoObtido.get(i).toString()) + 2, Integer.parseInt(yCaminhoObtido.get(i).toString()) + 2, 1, 1);
                    }
                }
            } catch (java.lang.NullPointerException ex) {
                System.out.println("Line" + getLineNumber() + ": " + ex);
            }
        }
        return 1;
    }

    private int DesenharTextura(Graphics2D g2) {
        //Textura area de simulação
        TexturePaint paint = new TexturePaint(ImgTextura, new Rectangle2D.Double(0, 0, ImgTextura.getWidth(), ImgTextura.getHeight()));
        g2.setPaint(paint);
        g2.fillRect(0, 0, 800, 500);

        //Textura abaixo da area de simulação           
        paint = new TexturePaint(ImgTextura1, new Rectangle2D.Double(0, 0, ImgTextura1.getWidth(), ImgTextura1.getHeight()));
        g2.setPaint(paint);
        g2.fillRect(0, 500, ImgTextura1.getWidth(), ImgTextura1.getHeight());
        return 1;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        //Desnhar a textura
        DesenharTextura(g2);

        //Desenhar a message
        DesenharString(g2, message);

        //Desenhar arena       
        DesenhaArena(g2);

        //Desenha os quadradinhos que já foram analizados
        AnalisePosicoes(g2);

        //Desenhar a obtençao do caminho
        DesenharCaminhoObtido(g2);

        //Desenho dos leos em andamento
        //  LeosAndamento(g2);
        //Desenhar os botoes
        DesenhaBotoes(g2);

        //Aviso com legenda de algum objecto
        DesenharString(g2, message);

        //DesenhaAlvo
        DesenhaAlvo(g2);

        //Depois do botão "Ver simulação" ser premido ATENÇÃO: ISTO TEM DE FICAR SEMPRE DEPOIS DO DESENHO DO ALVO PARA NAO SOBRE POR O ALVO EM CIMA DO CAO
        BotaoVerSimulacaoPremido(g2);

    }

    @Override
    public void run() {
        while (true) {
            repaint();
            if (ms <= 1000 && ms > 0 && aFuncionar == 1) {
                if (troca == false) {
                    corDesricao = Color.GREEN;
                    ms -= 20;
                } else {
                    corDesricao = Color.GRAY;
                    ms -= 20;
                }
            } else if (ms <= 0 && aFuncionar == 1) {
                troca = !troca;
                ms = 1000;
            } else if (aFuncionar == 0) {
                corDesricao = Color.GRAY;
            }
            try {
                Thread.sleep(35);
            } catch (InterruptedException ex) {
                Logger.getLogger("Line" + getLineNumber() + ": " + Panel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static int getLineNumber() {
        return Thread.currentThread().getStackTrace()[2].getLineNumber();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.          
        try {
            if (calculo_em_andamento == false) {//Quando esta flag está activo é porque o algoritmo está em utilização
                xAlvo = e.getX();
                yAlvo = e.getY();
                rodaOsso = true;//activa a rotação do osso
            } else if ((xRato >= xPararSimulação && xRato <= xPararSimulação + 130)//Este if é para pintar o botão assim que o rato estiver em cima
                    && (yRato >= yPararSimulação && yRato <= yPararSimulação + 40)) {
                PararSimulacao = true;
                rodaOsso = true;//activa a rotação do osso
            }
            if ((xRato >= xVerSimulação && xRato <= xVerSimulação + 130)//Se botão "Ver Simulação" premido então
                    && (yRato >= yVerSimulação && yRato <= yVerSimulação + 40) && aFuncionar == 0 && xAlvoDepoisDeDesenhado != xAlvoAntigo && yAlvoDepoisDeDesenhado != yAlvoAntigo) {
                verSimulacao = true;//Foi clicado o botão de ver a simulação               
            }
            if ((xRato >= xImprimir && xRato <= xImprimir + 115 && verImprimir == true && calculo_em_andamento == false)//Este if é para pintar o botão assim que o rato estiver em cima
                    && (yRato >= yImprimir && yRato <= yImprimir + 40)) {
                BotaoImprimirPremido();
            }
            flag = false;//Se antes havia parede, depois do clique do utilizador assume-se que nao ha parede. Depois é feito o teste no desenho
        } catch (Exception ex) {
            System.out.println("Line" + getLineNumber() + ": " + ex);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {

// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseDragged(MouseEvent e) {

//  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        xRato = e.getX();
        yRato = e.getY();
        try {
            if (startClique == true) {//Isto para evitar mostrar texto antes do primeiro clique do utilizador
                if (calculo_conluido == false && calculo_em_andamento == false) {
                    if (xRato >= xCao - (widthCao / 2) && xRato <= xCao + (widthCao / 2)
                            && yRato >= yCao - (heightCao / 2) && yRato <= yCao + (heightCao / 2)) {//Se verifica se existe parede uma vez que a posição do rato está dentro da area da simulação
                        message = "A posição do rato representa o Agente de Procura";
                    } else if (xRato >= xAlvoAntigo - (widthAlvo / 2) && xRato <= xAlvoAntigo + (widthAlvo / 2)
                            && yRato >= yAlvoAntigo - (heightAlvo / 2) && yRato <= yAlvoAntigo + (heightAlvo / 2)) {
                        message = "A posição do rato representa o Alvo";
                    } else if ((xRato >= xVerSimulação && xRato <= xVerSimulação + 130)//Este if é para pintar o botão assim que o rato estiver em cima
                            && (yRato >= yVerSimulação && yRato <= yVerSimulação + 40)) {
                        message = "Veja a simulação do percurso aplicando o algoritmo A*";
                    } else if ((xRato >= xImprimir && xRato <= xImprimir + 115)//Este if é para pintar o botão assim que o rato estiver em cima
                            && (yRato >= yImprimir && yRato <= yImprimir + 40) && verImprimir == true) {
                        message = "Faça uma análise manual a todos os calculos e escolhas efectuadas do ultimo percurso";
                    } else {
                        message = "";
                    }
                    flag = false;
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Line" + getLineNumber() + ": " + ex);
        }
    }
}
