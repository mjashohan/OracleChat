package com.Shohan.OracleChat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.util.function.Consumer;

@Route("")
@Push
@StyleSheet("frontend://styles/styles.css")
@PWA(name = "Vaadin Application",
        shortName = "Vaadin App",
        description = "This is a simple chatbox.",
        enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {

    private final UnicastProcessor<ChatMessage> publisher;
    private final Flux<ChatMessage> messages;
    private String username;

    public MainView(UnicastProcessor<ChatMessage> publisher,
                    Flux<ChatMessage> messages) {
        this.publisher = publisher;
        this.messages = messages;
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setSizeFull();
        addClassName("main-view");

        H1 header = new H1("Oracle Chat");
        header.getElement().getThemeList().add("dark");

        add(header);

        askUserName();
    }
    public void askUserName(){
        HorizontalLayout usernameLayout = new HorizontalLayout();

        TextField usernameField = new TextField();
        Button startButton = new Button("Start Chatting");
        usernameLayout.add(usernameField, startButton);

        startButton.addClickListener(buttonClickEvent -> {
            username = usernameField.getValue();
            remove(usernameLayout);
            showChat();
        });
        add(usernameLayout);
    }

    private void showChat() {
        MessageList messageList = new MessageList();

        add(messageList, createInputLayout());
        expand(messageList);

        messages.subscribe((Consumer<? super ChatMessage>) message -> {
            getUI().ifPresent(ui ->
                    ui.access(() ->
                            messageList.add(
                                    new Paragraph(message.getFrom() + ": " +
                                            message.getMessage())
                            )
                    ));
        });
    }

    private Component createInputLayout() {
        HorizontalLayout inputLayout = new HorizontalLayout();
        inputLayout.setWidth("100%");
        TextField messageField = new TextField();
        Button  sendButton = new Button("Send");
        sendButton.getElement().getThemeList().add("primary");

        inputLayout.add(messageField, sendButton);
        inputLayout.expand(messageField);

        sendButton.addClickListener(buttonClickEvent -> {
           publisher.onNext(new ChatMessage(username, messageField.getValue()));
           messageField.clear();
           messageField.focus();
        });
        messageField.focus();

        return inputLayout;
    }

}
