import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WordFrequencyCounter
{

    public Map<String, Integer> countWords(Path filePath) throws IOException
    {
        try
        {
            System.out.println("Используется полное чтение файла.");
            return countWordsСompletely(filePath);
        }
        catch (OutOfMemoryError e)
        {
            System.out.println("Используется потоковое чтение файла. Недостаточно места для полного чтения.");
            return countWordsParts(filePath);
        }
    }


    public Map<String, Integer> countWordsСompletely(Path filePath) throws IOException
    {
        Map<String, Integer> newMap = new HashMap<>();

        // читаем файл полностью. Записываем в строку
        String allCont = Files.readString(filePath);


        // удаляем лишние символы
        String moderAllCont = allCont.toLowerCase().replaceAll("[^a-zA-Zа-яА-ЯёЁ\\s]", "");

        // делим строку на слова. Используем для разделения пробелы
        String[] words = moderAllCont.split(" ");

        // записываем в map. Увеличиваем счетчик
        for(int i = 0; i < words.length; i++)
        {
            String word = words[i];
            if(!word.equals(""))
            {
                newMap.put(word, newMap.getOrDefault(word, 0) + 1);
            }
        }

        return newMap;
    }
    //чтение частями
    public Map<String, Integer> countWordsParts(Path filePath) throws IOException
    {
        Map<String, Integer> newMap = new HashMap<>();

        try (BufferedReader newBuffer = Files.newBufferedReader(filePath))
        {



            char[] buffer = new char[4096];
            StringBuilder wordPart = new StringBuilder(); // хранение неполного слова

            int readed = newBuffer.read(buffer, 0, buffer.length); // чтение

            while (readed != -1)
            {
                String part = wordPart.toString() + new String(buffer, 0, readed);
                part = part.replaceAll("\\r?\\n", " ");

                int space = lastF(part);

                if (space == -1)
                {
                    wordPart = new StringBuilder(part);
                }
                else
                {
                    String fullPart = part.substring(0, space); // полные слова
                    String lastPart = part.substring(space + 1); // неполное слово

                    if (!fullPart.isEmpty())
                    {
                        String cleanText = fullPart.toLowerCase().replaceAll("[^a-zA-Zа-яА-ЯёЁ\\s]", " ");
                        String[] words = cleanText.split(" ");

                        for (int i = 0; i < words.length; i++)
                        {
                            String word = words[i];
                            if (!word.equals(""))
                            {
                                newMap.put(word, newMap.getOrDefault(word, 0) + 1);
                            }
                        }
                    }
                    wordPart = new StringBuilder(lastPart);
                }

                readed = newBuffer.read(buffer, 0, buffer.length);

            }
            if (wordPart.length() > 0)
            {
                String cleanText = wordPart.toString().toLowerCase().replaceAll("[^a-zA-Zа-яА-ЯёЁ\\s]", " ");
                String[] words = cleanText.split(" ");

                for (int i = 0; i < words.length; i++)
                {
                    String word = words[i];
                    if (!word.equals(""))
                    {
                        newMap.put(word, newMap.getOrDefault(word, 0) + 1);
                    }
                }
            }
        }
        return newMap;
    }

    private int lastF(String text)
    {
        for (int i = text.length() - 1; i >= 0; i--)
        {
            char c = text.charAt(i);
            if (!Character.isLetter(c))
            {
                return i;
            }
        }
        return -1;
    }



    public void printFrequencies(Map<String, Integer> frequencies)
    {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(frequencies.entrySet());

        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<String, Integer> temp : list)
        {
            System.out.println(temp.getKey() + " " + temp.getValue());
        }
    }



    public static void main(String[] args)
    {
        WordFrequencyCounter count = new WordFrequencyCounter();

        Scanner scanner = new Scanner(System.in);
        WordFrequencyCounter counter = new WordFrequencyCounter();

        System.out.print("Введите путь к файлу ");
        String fileName = scanner.nextLine().trim();

        Path filePath = Paths.get(fileName);

        try {
            Map<String, Integer> result = counter.countWords(filePath);
            counter.printFrequencies(result);
        }
        catch (IOException e)
        {
            System.err.println("Ошибка при чтении файла " + e.getMessage());
        }

        scanner.close();

    }
}