package com.example.application.views.cliente;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.example.application.model.Cliente;
import com.example.application.model.Funcionario;
import com.example.application.repository.ClienteRepository;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PageTitle("Cliente")
@Route(value = "my-view", layout = MainLayout.class)
public class ClienteView extends Composite<VerticalLayout> {
    private static final Logger logger = LoggerFactory.getLogger(ClienteView.class);

    private ClienteRepository clienteRepository;
    private Grid<Cliente> grid = new Grid(Cliente.class, false);
    private TextField nome = new TextField("Nome");
    private TextField cpf = new TextField("CNPJ/CPF");
    private TextField rg = new TextField("IE/RG");
    private TextField telefone = new TextField("Telefone");
    private Long clienteId;
    private TabSheet tabSheet;

    public ClienteView() {
        try {
            clienteRepository = new ClienteRepository();

            tabSheet = new TabSheet();
            getContent().setWidth("100%");
            getContent().getStyle().set("flex-grow", "1");
            tabSheet.setWidth("100%");
            setTabSheetSampleData(tabSheet);
            getContent().add(tabSheet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //This method sets up two tabs
    private void setTabSheetSampleData(TabSheet tabSheet) {
        Div clientesContent = createClientesContent();
        tabSheet.add("Clientes", clientesContent);

        Div addClientesContent = createAddClientesContent();
        tabSheet.add("Adicionar Cliente", addClientesContent);
    }

    //This method creates the content for the "Clientes" tab, which consists of a form to see all the clients and search for a specific client
    private Div createClientesContent(){
        Div clientesContentDiv = new Div();
        Div space = new Div();
        space.setHeight("15px");

        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout layoutRow = new HorizontalLayout();
        TextField textField = new TextField("Pesquisar");
        Button buttonPrimary = new Button();

        buttonPrimary.addClickListener(event -> {
            String pesquisa = textField.getValue().trim();
            List<Cliente> resultados;

            if(pesquisa.isEmpty()){
                resultados = clienteRepository.pesquisarTodos();
            }else{
                resultados = clienteRepository.pesquisarCliente(pesquisa);
            }

            grid.setItems(resultados);
        });

        //For a better interface
        textField.setPlaceholder("Nome, CNPJ/CPF ou IE/RG");
        textField.setWidth("250px");
        layoutRow.setWidthFull();
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setAlignItems(Alignment.END);
        layoutRow.setJustifyContentMode(JustifyContentMode.END);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("70px");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonPrimary.setIcon(VaadinIcon.SEARCH.create());
        buttonPrimary.getStyle().set("border-radius", "50%");
        textField.addClassName("rounded-text-field");
        layoutRow.add(textField, buttonPrimary);
        grid.setAllRowsVisible(true);
        grid.addClassName("borderless-grid");

        buttonPrimary.addClickListener(cliente -> {
            Notification.show("Pesquisar por: " + textField.getValue());
        });

        grid.addColumn(Cliente::getNome).setHeader("Nome");
        grid.addColumn(Cliente::getTelefone).setHeader("Telefone");
 
        grid.addComponentColumn(cliente -> {
            Button delete = new Button(VaadinIcon.TRASH.create(), e -> {
                Dialog confirm = new Dialog();
                confirm.setHeaderTitle("Confirmar Exclusão");
                VerticalLayout content = new VerticalLayout();
                content.add(new Text("Você tem certeza que deseja excluir o cliente " + cliente.getNome() + "?"));

                Button confirmButton = new Button("Confirmar", event -> {
                    deleteCliente(cliente);
                    confirm.close();
                });

                Button cancel = new Button("Cancelar", event -> confirm.close());

                confirm.getFooter().add(confirmButton, cancel);
                confirm.add(content);
                confirm.open();
            });
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return delete;
        }).setHeader("Ações");

        grid.setDetailsVisibleOnClick(false);
        grid.setItemDetailsRenderer(createPersonDetailsRenderer());

        grid.addItemClickListener(event -> {
            Cliente cliente = event.getItem();
            grid.setDetailsVisible(cliente, !grid.isDetailsVisible(cliente));
        });

        grid.addItemDoubleClickListener(event -> {
            Cliente cliente = event.getItem();
            editCliente(cliente);
            tabSheet.setSelectedIndex(1);
        });

        List<Cliente> listaDeClientes = Collections.emptyList();
        try {
            listaDeClientes = clienteRepository.pesquisarTodos();
        } catch (Exception e) {
            Notification.show("Erro inesperado ao consultar os clientes", 3000, Notification.Position.MIDDLE);
            logger.error("Erro inesperado ao consultar os clientes", e);
        }
        grid.setItems(listaDeClientes);

        layout.add(layoutRow, space, grid);
        clientesContentDiv.add(layout);

        return clientesContentDiv;
    }

    //This method creates the content for the "Adicionar Cliente" tab, which consists of a form to add a new client.
    private Div createAddClientesContent(){
        Div addClientesContentDiv = new Div();
        Div space = new Div();
        space.setHeight("10px");
        Div space1 = new Div();
        space1.setHeight("10px");

        VerticalLayout layout = new VerticalLayout();
        VerticalLayout layout2 = new VerticalLayout();
        VerticalLayout layout3 = new VerticalLayout();
        FormLayout formLayout2Col = new FormLayout();
        nome = new TextField("Nome");
        cpf = new TextField("CNPJ/CPF");
        rg = new TextField("IE/RG");
        telefone = new TextField("Telefone");

        cpf.addValueChangeListener(event -> {
            String value = event.getValue().replaceAll("[^\\d]", "");
            if (value.length() <= 11) {
                value = value.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
            }
            cpf.setValue(value);
        });
        cpf.setMaxLength(14);

        rg.addValueChangeListener(event -> {
            String value = event.getValue().replaceAll("[^\\d]", "");
            if (value.length() <= 9) {
                value = value.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{1})", "$1.$2.$3-$4");
            }
            rg.setValue(value);
        });
        rg.setMaxLength(12);

        telefone.addBlurListener(event -> {
            String value = telefone.getValue().replaceAll("[^\\d]", "");
        
            if (value.length() == 10) {
                value = value.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
            } else if (value.length() == 11) {
                value = value.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
            }
        
            telefone.setValue(value);
        });
        telefone.setMaxLength(15);

        Button saveButton = new Button("Salvar", event -> {
            if (nome.isEmpty() || telefone.isEmpty()) {
                Notification.show("Preencha os campos obrigatórios: Nome e Telefone", 3000, Notification.Position.MIDDLE);
                return;
            }
            String nomeCliente = nome.getValue();
            String cpfCliente = cpf.isEmpty() ? "" : cpf.getValue();
            String rgCliente = rg.isEmpty() ? "" : rg.getValue();
            String telefoneCliente = telefone.getValue();
        
            Cliente cliente = new Cliente(nomeCliente, cpfCliente, rgCliente, telefoneCliente);
            cliente.setId(clienteId);
        
            boolean sucesso = false;
            try {
                if (clienteId != null && clienteId > 0) {
                    sucesso = clienteRepository.alterar(cliente);
                    if (sucesso) {
                        Notification.show("Cliente atualizado com sucesso!");
                        clearForm();
                    } else {
                        Notification.show("Erro ao atualizar o cliente", 3000, Notification.Position.MIDDLE);
                    }
                } else {
                    sucesso = clienteRepository.inserir(cliente);
                    if (sucesso) {
                        Notification.show("Cliente salvo com sucesso!");
                        clearForm();
                    } else {
                        Notification.show("Erro ao salvar o cliente", 3000, Notification.Position.MIDDLE);
                    }
                }
                if (sucesso) {
                    tabSheet.setSelectedIndex(0);
                    refreshGrid();
                }
            } catch (Exception e) {
                Notification.show("Erro ao acessar o banco de dados: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        });

        //For a better interface
        nome.addClassName("rounded-text-field");
        cpf.addClassName("rounded-text-field");
        rg.addClassName("rounded-text-field");
        telefone.addClassName("rounded-text-field");
        nome.setRequiredIndicatorVisible(true);
        telefone.setRequiredIndicatorVisible(true);
        layout3.setAlignItems(FlexComponent.Alignment.END);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle().set("border-radius", "25px");
        layout2.getStyle().set("border-radius", "15px");
        layout2.getStyle().set("border", "1px solid #ccc");
        layout2.getStyle().set("box-shadow", "0 0 2px rgba(0 , 0, 0, 0.2)");
        layout2.setWidth("1100px");
        layout2.getStyle().set("margin", "0 auto");
        
        formLayout2Col.add(nome, cpf, rg, telefone);
        layout2.add(formLayout2Col, space);
        layout3.add(saveButton);
        layout.add(layout2, layout3);
        addClientesContentDiv.add(space1, layout);

        return addClientesContentDiv;
    }

    private void refreshGrid(){
        List<Cliente> clientes = clienteRepository.pesquisarTodos();
        grid.setItems(clientes);
    }

    private void deleteCliente(Cliente cliente) {
        try {
            boolean success = clienteRepository.excluir(cliente);
            if (success) {
                refreshGrid();
            } else {
                System.out.println("Erro ao excluir cliente");
                Notification.show("Erro ao excluir cliente", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            // Tratar outros erros inesperados
            System.out.println("Erro inesperado ao excluir cliente");
            Notification.show("Erro inesperado ao excluir cliente", 3000, Notification.Position.MIDDLE);
            logger.error("Erro inesperado ao excluir cliente", e);
        }
    }
    

    private void editCliente(Cliente cliente) {
        clienteId = cliente.getId();
        nome.setValue(cliente.getNome());
        rg.setValue(String.valueOf(cliente.getRg()));
        cpf.setValue(String.valueOf(cliente.getCpf()));
        telefone.setValue(String.valueOf(cliente.getTelefone()));
    }

    private void clearForm() {
        clienteId = null;
        nome.clear();
        rg.clear();
        cpf.clear();
        telefone.clear();
    }

    private static ComponentRenderer<HorizontalLayout, Cliente> createPersonDetailsRenderer() {
        return new ComponentRenderer<>(cliente -> {
            HorizontalLayout detailsLayout = new HorizontalLayout();
            detailsLayout.setSpacing(true);
            detailsLayout.setPadding(true);
            detailsLayout.addClassName("details-layout");
    
            TextField cpfField = new TextField("CNPJ/CPF");
            cpfField.setValue(cliente.getCpf());
            cpfField.setReadOnly(true);
            cpfField.addClassName("rounded-text-field");
    
            TextField rgField = new TextField("IE/RG");
            rgField.setValue(cliente.getRg());
            rgField.setReadOnly(true);
            rgField.addClassName("rounded-text-field");
    
            detailsLayout.add(cpfField, rgField);
    
            return detailsLayout;
        });
    }

}