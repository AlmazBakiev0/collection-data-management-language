package PrimitiveDataManagementLanguage;

import java.util.*;

public class RequestHandler {

    private final List<Map<String, Object>> list = new ArrayList<>();

    public RequestHandler() {
    }

    public List<Map<String, Object>> execute(String request) throws Exception {

        String[] command = request.split(" ", 3);
        if (command.length > 2 && (command[1].equalsIgnoreCase("values")
                || command[1].equalsIgnoreCase("where"))) {
            command[2] = command[2].replaceAll("Т", "")
                    .replaceAll("С", "")
                    .replaceAll("'", "");
        }

        if (command[0].equalsIgnoreCase("insert")) {
            insert(command[2]);
        } else if (command[0].equalsIgnoreCase("update")) {
            update(command[2]);
        } else if (command[0].equalsIgnoreCase("select")) {
            if (command.length < 3) {
                return list;
            } else {
                return select(command[2]);
            }
        } else if (command[0].equalsIgnoreCase("delete")) {
            if (command.length < 3) {
                list.clear();
            } else {
                delete(command[2]);
            }
        } else {
            throw new Exception(command[0] + " - такой команды нет!!!");
        }
        return list;
    }

    private void insert(String values) {

        Map<String, Object> row = new HashMap<>();
        String[] valuesArray = values.replaceAll("\s", "").split(",");

        for (String value : valuesArray) {
            rowPut(value, row);
        }
        list.add(row);
    }

    private void update(String values) {

        List<Map<String, Object>> updateList = new ArrayList<>(list);

        if (!values.contains(" where ")) {
            String[] valuesForUpdate = values.replaceAll("\s", "").split(",");
            list.forEach(row -> {
                for (String value : valuesForUpdate) {
                    rowPut(value, row);
                }
            });
            return;
        }

        String[] valuesAndConditions = values.split(" where ");
        String[] valuesForUpdate = valuesAndConditions[0].replaceAll("\s", "").split(",");

        sample(valuesAndConditions[1], updateList);

        list.forEach(row -> {
            for (Map<String, Object> updateRow : updateList) {
                if (row.equals(updateRow)) {
                    for (String value : valuesForUpdate) {
                        rowPut(value, row);
                    }
                }
            }
        });
    }

    private List<Map<String, Object>> select(String values) {

        List<Map<String, Object>> selectList = new ArrayList<>(list);

        sample(values, selectList);

        return selectList;
    }

    private void delete(String values) {

        List<Map<String, Object>> deleteList = new ArrayList<>(list);

        sample(values, deleteList);

        list.removeIf(row -> {
            for (Map<String, Object> deleteRow : deleteList) {
                if (row.equals(deleteRow)) {
                    return true;
                }
            }
            return false;
        });
    }

    public void output(List<Map<String, Object>> list) {
        list.forEach(row -> System.out.printf("id: %-5s lastName: %-15s age: %-5s cost: %-10s active: %-10s%n",
                row.get("id"), row.get("lastName"), row.get("age"), row.get("cost"), row.get("active")));
    }

    private void rowPut(String values, Map<String, Object> row) {
        try {
            String[] valueArray = values.split("=");
            for (int i = 0; i < valueArray.length; i++) {
                if (valueArray[i].equalsIgnoreCase("id")) {
                    valueArray[i] = "id";
                } else if (valueArray[i].equalsIgnoreCase("age")) {
                    valueArray[i] = "age";
                } else if (valueArray[i].equalsIgnoreCase("cost")) {
                    valueArray[i] = "cost";
                } else if (valueArray[i].equalsIgnoreCase("active")) {
                    valueArray[i] = "active";
                } else if (valueArray[i].equalsIgnoreCase("lastName")) {
                    valueArray[i] = "lastName";
                }
            }

            if (valueArray[1].equals("null")) {
                row.remove(valueArray[0]);
                return;
            }

            if (valueArray[0].equalsIgnoreCase("id") ||
                    valueArray[0].equalsIgnoreCase("age")) {
                row.put(valueArray[0], Long.parseLong(valueArray[1]));
            } else if (valueArray[0].equalsIgnoreCase("cost")) {
                row.put(valueArray[0], Double.parseDouble(valueArray[1]));
            } else if (valueArray[0].equalsIgnoreCase("active")) {

                if (valueArray[1].equalsIgnoreCase("false") ||
                        valueArray[1].equalsIgnoreCase("true")) {
                    row.put(valueArray[0], Boolean.parseBoolean(valueArray[1]));
                }
            } else if (valueArray[0].equalsIgnoreCase("lastName")) {
                if (hasDigit(valueArray[1])) {
                    row.put(valueArray[0], valueArray[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void operations(String value, List<Map<String, Object>> selectList) {
        try {
            if (value.contains(">=")) {
                moreOrEqual(value, selectList);
            } else if (value.contains("<=")) {
                lessOrEqual(value, selectList);
            } else if (value.contains("!=")) {
                notEqual(value, selectList);
            } else if (value.contains("=")) {
                isEqual(value, selectList);
            } else if (value.contains("<")) {
                less(value, selectList);
            } else if (value.contains(">")) {
                more(value, selectList);
            }

            if (value.contains("ilike")) {
                iLike(value, selectList);
            } else if (value.contains("like")) {
                like(value, selectList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sample(String values, List<Map<String, Object>> sampleList) {

        if (values.contains(" and ")) {
            String[] valuesArray = values.replace(" and ", ",")
                    .replaceAll("\s", "").split(",");

            for (String value : valuesArray) {
                operations(value, sampleList);
            }
        } else if (values.contains(" or ")) {
            Set<Map<String, Object>> selectSet = new LinkedHashSet<>();
            String[] valuesArray = values.replace(" or ", ",")
                    .replaceAll("\s", "").split(",");

            for (String value : valuesArray) {
                sampleList.addAll(0, list);
                operations(value, sampleList);

                selectSet.addAll(sampleList);
                sampleList.clear();
            }
            sampleList.clear();
            sampleList.addAll(selectSet);
        } else {
            operations(values, sampleList);
        }
    }

    private void like(String value, List<Map<String, Object>> selectList) {
        String[] valueArray = value.split("like");
        if (valueArray[0].equalsIgnoreCase("lastName")) {
            if (hasDigit(valueArray[1])) {
                selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                        !((String) row.get(valueArray[0])).contains(valueArray[1].replaceAll("%", "")));
            }
        }
    }

    private void iLike(String value, List<Map<String, Object>> selectList) {
        String[] valueArray = value.split("ilike");
        if (valueArray[0].equalsIgnoreCase("lastName")) {
            if (hasDigit(valueArray[1])) {
                selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                        !(((String) row.get(valueArray[0]))
                                .contains(valueArray[1].replaceAll("%", "").toUpperCase()) ||
                                ((String) row.get(valueArray[0]))
                                        .contains(valueArray[1].replaceAll("%", "").toLowerCase())));
            }
        }
    }

    private void more(String value, List<Map<String, Object>> selectList) {
        String[] valueArray = value.split(">");
        if (valueArray[0].equalsIgnoreCase("id") || valueArray[0].equalsIgnoreCase("age")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Long) row.get(valueArray[0]) <= Long.parseLong(valueArray[1]));
        } else if (valueArray[0].equalsIgnoreCase("cost")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Double) row.get(valueArray[0]) <= Double.parseDouble(valueArray[1]));
        }
    }

    private void less(String value, List<Map<String, Object>> selectList) {
        String[] valueArray = value.split("<");
        if (valueArray[0].equalsIgnoreCase("id") || valueArray[0].equalsIgnoreCase("age")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Long) row.get(valueArray[0]) >= Long.parseLong(valueArray[1]));
        } else if (valueArray[0].equalsIgnoreCase("cost")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Double) row.get(valueArray[0]) >= Double.parseDouble(valueArray[1]));
        }
    }

    private void isEqual(String value, List<Map<String, Object>> selectList) {
        String[] valueArray = value.split("=");
        if (valueArray[0].equalsIgnoreCase("id") || valueArray[0].equalsIgnoreCase("age")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Long) row.get(valueArray[0]) != Long.parseLong(valueArray[1]));
        } else if (valueArray[0].equalsIgnoreCase("cost")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Double) row.get(valueArray[0]) != Double.parseDouble(valueArray[1]));
        } else if (valueArray[0].equalsIgnoreCase("active")) {

            if (valueArray[1].equalsIgnoreCase("false") ||
                    valueArray[1].equalsIgnoreCase("true")) {
                selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                        !row.get(valueArray[0]).equals(Boolean.parseBoolean(valueArray[1])));
            }
        } else if (valueArray[0].equalsIgnoreCase("lastName")) {
            if (hasDigit(valueArray[1])) {
                selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                        !row.get(valueArray[0]).equals(valueArray[1]));
            }
        }
    }

    private void notEqual(String value, List<Map<String, Object>> selectList) {
        String[] valueArray = value.split("!=");
        if (valueArray[0].equalsIgnoreCase("id") || valueArray[0].equalsIgnoreCase("age")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Long) row.get(valueArray[0]) == Long.parseLong(valueArray[1]));
        } else if (valueArray[0].equalsIgnoreCase("cost")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Double) row.get(valueArray[0]) == Double.parseDouble(valueArray[1]));
        } else if (valueArray[0].equalsIgnoreCase("active")) {

            if (valueArray[1].equalsIgnoreCase("false") ||
                    valueArray[1].equalsIgnoreCase("true")) {
                selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                        row.get(valueArray[0]).equals(Boolean.parseBoolean(valueArray[1])));
            }
        } else if (valueArray[0].equalsIgnoreCase("lastName")) {
            if (hasDigit(valueArray[1])) {
                selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                        row.get(valueArray[0]).equals(valueArray[1]));
            }
        }
    }

    private void lessOrEqual(String value, List<Map<String, Object>> selectList) {
        String[] valueArray = value.split("<=");
        if (valueArray[0].equalsIgnoreCase("id") || valueArray[0].equalsIgnoreCase("age")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Long) row.get(valueArray[0]) > Long.parseLong(valueArray[1]));
        } else if (valueArray[0].equalsIgnoreCase("cost")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Double) row.get(valueArray[0]) > Double.parseDouble(valueArray[1]));
        }
    }

    private void moreOrEqual(String value, List<Map<String, Object>> selectList) {
        String[] valueArray = value.split(">=");
        if (valueArray[0].equalsIgnoreCase("id") || valueArray[0].equalsIgnoreCase("age")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Long) row.get(valueArray[0]) < Long.parseLong(valueArray[1]));
        } else if (valueArray[0].equalsIgnoreCase("cost")) {
            selectList.removeIf(row -> row.get(valueArray[0]) != null &&
                    (Double) row.get(valueArray[0]) < Double.parseDouble(valueArray[1]));
        }
    }
}