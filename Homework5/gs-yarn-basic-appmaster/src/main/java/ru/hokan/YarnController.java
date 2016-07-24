package ru.hokan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

@Controller
public class YarnController {

    private static final Log LOGGER = LogFactory.getLog(YarnController.class);

    @Autowired
    private AppmasterApplication application;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String processGetMethod(@Valid HTMLPostResponse post, Model model) {
        String amountOfRAM = post.getAmountOfRAM();
        String priority = post.getPriority();
        String numberOfContainers = post.getNumberOfContainers();

        LOGGER.info("In GET method");
        LOGGER.info("RAM: " + amountOfRAM);
        LOGGER.info("Priority: " + priority);
        LOGGER.info("NumberOfContainers: " + numberOfContainers);

        model.addAttribute("currentAmountOfRAM", amountOfRAM == null ? "" : amountOfRAM);
        model.addAttribute("currentPriority", priority == null ? "" : priority);
        model.addAttribute("currentNumberOfContainers", numberOfContainers == null ? "" : numberOfContainers);

        return "controller";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String processPostMethod(@Valid HTMLPostResponse post, BindingResult bindingResult, Model model) {
        String amountOfRAM = post.getAmountOfRAM();
        String priority = post.getPriority();
        String numberOfContainers = post.getNumberOfContainers();

        LOGGER.info("In POST method");
        LOGGER.info("RAM: " + amountOfRAM);
        LOGGER.info("Priority: " + priority);
        LOGGER.info("NumberOfContainers: " + numberOfContainers);

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentAmountOfRAM", application.getAmountOfRAMInMb());
            model.addAttribute("currentPriority", application.getPriopiry());
            model.addAttribute("currentNumberOfContainers", application.getNumberOfContainers());
            return "controller";
        }

        model.addAttribute("currentAmountOfRAM", amountOfRAM);
        model.addAttribute("currentPriority", priority);
        model.addAttribute("currentNumberOfContainers", numberOfContainers);

        application.runApplicationWithParameters(Integer.valueOf(amountOfRAM), Integer.valueOf(priority), Integer.valueOf(numberOfContainers));
        return "controller";
    }
}
