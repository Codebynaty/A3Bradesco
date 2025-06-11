package com.bradesco.pixmonitor.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TemporalFeaturesService {
    
    // Horários considerados suspeitos (madrugada)
    private static final Set<Integer> HORARIOS_SUSPEITOS = Set.of(0, 1, 2, 3, 4, 5);
    
    // Horários comerciais normais
    private static final Set<Integer> HORARIOS_COMERCIAIS = Set.of(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);
    
    // Feriados brasileiros fixos (simples implementação)
    private static final Set<String> FERIADOS_2025 = Set.of(
        "01-01", "04-21", "09-07", "10-12", "11-02", "11-15", "12-25"
    );
    
    /**
     * Extrai features temporais abrangentes de uma lista de transações
     */
    public Map<String, Object> extrairFeaturesTemporais(List<Map<String, Object>> transacoes) {
        Map<String, Object> features = new HashMap<>();
        
        if (transacoes == null || transacoes.isEmpty()) {
            return getFeaturesTemporaisVazias();
        }
        
        // 1. Features de Horário
        features.putAll(analisarPadroesHorarios(transacoes));
        
        // 2. Features de Dias da Semana
        features.putAll(analisarPadroesDiasSemana(transacoes));
        
        // 3. Features de Velocidade/Frequência
        features.putAll(analisarVelocidadeTransacoes(transacoes));
        
        // 4. Features de Sazonalidade
        features.putAll(analisarSazonalidade(transacoes));
        
        // 5. Features de Anomalias Temporais
        features.putAll(detectarAnomaliasTempo(transacoes));
        
        // 6. Score de Risco Temporal
        features.put("score_risco_temporal", calcularScoreRiscoTemporal(features));
        
        return features;
    }
    
    /**
     * Análise detalhada de padrões de horário
     */
    private Map<String, Object> analisarPadroesHorarios(List<Map<String, Object>> transacoes) {
        Map<String, Object> features = new HashMap<>();
        
        // Extrair horários
        List<Integer> horarios = transacoes.stream()
                .map(t -> ((LocalDateTime) t.get("data_transacao")).getHour())
                .collect(Collectors.toList());
        
        // Estatísticas básicas
        Map<Integer, Long> distribuicaoHorarios = horarios.stream()
                .collect(Collectors.groupingBy(h -> h, Collectors.counting()));
        
        // Horário mais frequente
        int horarioPredominante = distribuicaoHorarios.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(12);
        
        // Variabilidade de horários (desvio padrão)
        double mediaHorario = horarios.stream().mapToInt(Integer::intValue).average().orElse(12.0);
        double desvioHorario = Math.sqrt(
                horarios.stream()
                        .mapToDouble(h -> Math.pow(h - mediaHorario, 2))
                        .average().orElse(0.0)
        );
        
        // Contagens por categoria de horário
        long transacoesMadrugada = horarios.stream()
                .filter(HORARIOS_SUSPEITOS::contains)
                .count();
        
        long transacoesComerciais = horarios.stream()
                .filter(HORARIOS_COMERCIAIS::contains)
                .count();
        
        long transacoesNoturnas = horarios.stream()
                .filter(h -> h >= 19 && h <= 23)
                .count();
        
        // Percentuais
        double percMadrugada = (double) transacoesMadrugada / transacoes.size() * 100;
        double percComerciais = (double) transacoesComerciais / transacoes.size() * 100;
        double percNoturnas = (double) transacoesNoturnas / transacoes.size() * 100;
        
        // Concentração (entropia dos horários)
        double entropiaHorarios = calcularEntropia(distribuicaoHorarios, transacoes.size());
        
        features.put("horario_predominante", horarioPredominante);
        features.put("media_horario", Math.round(mediaHorario * 100.0) / 100.0);
        features.put("desvio_horario", Math.round(desvioHorario * 100.0) / 100.0);
        features.put("transacoes_madrugada", (int) transacoesMadrugada);
        features.put("transacoes_comerciais", (int) transacoesComerciais);
        features.put("transacoes_noturnas", (int) transacoesNoturnas);
        features.put("perc_madrugada", Math.round(percMadrugada * 100.0) / 100.0);
        features.put("perc_comerciais", Math.round(percComerciais * 100.0) / 100.0);
        features.put("perc_noturnas", Math.round(percNoturnas * 100.0) / 100.0);
        features.put("entropia_horarios", Math.round(entropiaHorarios * 100.0) / 100.0);
        features.put("horarios_unicos", distribuicaoHorarios.size());
        
        return features;
    }
    
    /**
     * Análise de padrões por dias da semana
     */
    private Map<String, Object> analisarPadroesDiasSemana(List<Map<String, Object>> transacoes) {
        Map<String, Object> features = new HashMap<>();
        
        // Extrair dias da semana
        List<DayOfWeek> diasSemana = transacoes.stream()
                .map(t -> ((LocalDateTime) t.get("data_transacao")).getDayOfWeek())
                .collect(Collectors.toList());
        
        // Contagens
        long transacoesFinsSemanaa = diasSemana.stream()
                .filter(dia -> dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY)
                .count();
        
        long transacoesUteis = transacoes.size() - transacoesFinsSemanaa;
        
        // Distribuição por dia da semana
        Map<DayOfWeek, Long> distribuicaoDias = diasSemana.stream()
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));
        
        // Dia mais ativo
        DayOfWeek diaMaisAtivo = distribuicaoDias.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.MONDAY);
        
        // Regularidade (concentração em poucos dias)
        double entropiaDias = calcularEntropia(
                distribuicaoDias.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().getValue(), Map.Entry::getValue)),
                transacoes.size()
        );
        
        // Percentuais
        double percFinsSemanaa = (double) transacoesFinsSemanaa / transacoes.size() * 100;
        double percUteis = (double) transacoesUteis / transacoes.size() * 100;
        
        features.put("transacoes_fins_semana", (int) transacoesFinsSemanaa);
        features.put("transacoes_dias_uteis", (int) transacoesUteis);
        features.put("perc_fins_semana", Math.round(percFinsSemanaa * 100.0) / 100.0);
        features.put("perc_dias_uteis", Math.round(percUteis * 100.0) / 100.0);
        features.put("dia_mais_ativo", diaMaisAtivo.toString());
        features.put("entropia_dias", Math.round(entropiaDias * 100.0) / 100.0);
        features.put("dias_unicos_atividade", distribuicaoDias.size());
        
        return features;
    }
    
    /**
     * Análise de velocidade e frequência das transações
     */
    private Map<String, Object> analisarVelocidadeTransacoes(List<Map<String, Object>> transacoes) {
        Map<String, Object> features = new HashMap<>();
        
        if (transacoes.size() < 2) {
            features.put("intervalo_medio_segundos", 0);
            features.put("transacoes_rapidas_1min", 0);
            features.put("transacoes_rapidas_5min", 0);
            features.put("rajadas_suspeitas", 0);
            return features;
        }
        
        // Ordenar por data
        List<LocalDateTime> datasOrdenadas = transacoes.stream()
                .map(t -> (LocalDateTime) t.get("data_transacao"))
                .sorted()
                .collect(Collectors.toList());
        
        // Calcular intervalos entre transações
        List<Long> intervalosSegundos = new ArrayList<>();
        for (int i = 1; i < datasOrdenadas.size(); i++) {
            long segundos = ChronoUnit.SECONDS.between(datasOrdenadas.get(i-1), datasOrdenadas.get(i));
            intervalosSegundos.add(segundos);
        }
        
        // Estatísticas de intervalos
        double intervaloMedio = intervalosSegundos.stream()
                .mapToLong(Long::longValue)
                .average().orElse(0.0);
        
        long intervaloMinimo = intervalosSegundos.stream()
                .mapToLong(Long::longValue)
                .min().orElse(0L);
        
        long intervaloMaximo = intervalosSegundos.stream()
                .mapToLong(Long::longValue)
                .max().orElse(0L);
        
        // Transações muito rápidas (suspeitas)
        long transacoesRapidas1Min = intervalosSegundos.stream()
                .filter(intervalo -> intervalo <= 60)
                .count();
        
        long transacoesRapidas5Min = intervalosSegundos.stream()
                .filter(intervalo -> intervalo <= 300)
                .count();
        
        long transacoesRapidas10Seg = intervalosSegundos.stream()
                .filter(intervalo -> intervalo <= 10)
                .count();
        
        // Detectar rajadas (múltiplas transações em sequência rápida)
        int rajadasSuspeitas = detectarRajadas(intervalosSegundos);
        
        // Frequência por hora do dia
        Map<String, Object> frequenciaPorHora = calcularFrequenciaPorHora(transacoes);
        
        features.put("intervalo_medio_segundos", Math.round(intervaloMedio));
        features.put("intervalo_minimo_segundos", intervaloMinimo);
        features.put("intervalo_maximo_segundos", intervaloMaximo);
        features.put("transacoes_rapidas_1min", (int) transacoesRapidas1Min);
        features.put("transacoes_rapidas_5min", (int) transacoesRapidas5Min);
        features.put("transacoes_rapidas_10seg", (int) transacoesRapidas10Seg);
        features.put("rajadas_suspeitas", rajadasSuspeitas);
        features.put("frequencia_por_hora", frequenciaPorHora);
        
        return features;
    }
    
    /**
     * Análise de sazonalidade e padrões calendário
     */
    private Map<String, Object> analisarSazonalidade(List<Map<String, Object>> transacoes) {
        Map<String, Object> features = new HashMap<>();
        
        // Analisar datas
        List<LocalDateTime> datas = transacoes.stream()
                .map(t -> (LocalDateTime) t.get("data_transacao"))
                .collect(Collectors.toList());
        
        // Transações em feriados
        long transacoesFeriados = datas.stream()
                .filter(this::isFeriado)
                .count();
        
        // Transações no início/fim do mês (dias 1-5 e 25-31)
        long transacoesInicioMes = datas.stream()
                .filter(data -> data.getDayOfMonth() <= 5)
                .count();
        
        long transacoesFimMes = datas.stream()
                .filter(data -> data.getDayOfMonth() >= 25)
                .count();
        
        // Período de análise
        if (!datas.isEmpty()) {
            LocalDateTime dataMin = datas.stream().min(LocalDateTime::compareTo).get();
            LocalDateTime dataMax = datas.stream().max(LocalDateTime::compareTo).get();
            long diasPeriodo = ChronoUnit.DAYS.between(dataMin, dataMax) + 1;
            
            features.put("dias_periodo_analise", (int) diasPeriodo);
            features.put("data_primeira_transacao", dataMin.toString());
            features.put("data_ultima_transacao", dataMax.toString());
        }
        
        // Percentuais
        double percFeriados = transacoes.size() > 0 ? 
                (double) transacoesFeriados / transacoes.size() * 100 : 0.0;
        double percInicioMes = transacoes.size() > 0 ? 
                (double) transacoesInicioMes / transacoes.size() * 100 : 0.0;
        double percFimMes = transacoes.size() > 0 ? 
                (double) transacoesFimMes / transacoes.size() * 100 : 0.0;
        
        features.put("transacoes_feriados", (int) transacoesFeriados);
        features.put("transacoes_inicio_mes", (int) transacoesInicioMes);
        features.put("transacoes_fim_mes", (int) transacoesFimMes);
        features.put("perc_feriados", Math.round(percFeriados * 100.0) / 100.0);
        features.put("perc_inicio_mes", Math.round(percInicioMes * 100.0) / 100.0);
        features.put("perc_fim_mes", Math.round(percFimMes * 100.0) / 100.0);
        
        return features;
    }
    
    /**
     * Detecta anomalias temporais específicas
     */
    private Map<String, Object> detectarAnomaliasTempo(List<Map<String, Object>> transacoes) {
        Map<String, Object> features = new HashMap<>();
        
        List<LocalDateTime> datas = transacoes.stream()
                .map(t -> (LocalDateTime) t.get("data_transacao"))
                .collect(Collectors.toList());
        
        // Detectar padrões robotizados (mesmos minutos/segundos)
        Map<Integer, Long> distribuicaoMinutos = datas.stream()
                .map(LocalDateTime::getMinute)
                .collect(Collectors.groupingBy(m -> m, Collectors.counting()));
        
        Map<Integer, Long> distribuicaoSegundos = datas.stream()
                .map(LocalDateTime::getSecond)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        
        // Concentração suspeita em minutos específicos
        boolean padraoMinutosSuspeito = distribuicaoMinutos.values().stream()
                .anyMatch(count -> count > transacoes.size() * 0.3);
        
        boolean padraoSegundosSuspeito = distribuicaoSegundos.values().stream()
                .anyMatch(count -> count > transacoes.size() * 0.4);
        
        // Transações em horários muito específicos (potencial bot)
        long transacoesHorarioExato = datas.stream()
                .collect(Collectors.groupingBy(
                    data -> data.getHour() + ":" + 
                           String.format("%02d", data.getMinute()),
                    Collectors.counting()
                )).values().stream()
                .filter(count -> count > 1)
                .count();
        
        // Padrão de data/hora artificiais
        boolean dataHoraArtificial = datas.stream()
                .anyMatch(data -> 
                    data.getMinute() == 0 && data.getSecond() == 0 || // Horários "redondos"
                    data.getSecond() == data.getMinute() ||           // Padrões matemáticos
                    (data.getHour() == data.getMinute() && data.getMinute() < 12)
                );
        
        features.put("padrao_minutos_suspeito", padraoMinutosSuspeito);
        features.put("padrao_segundos_suspeito", padraoSegundosSuspeito);
        features.put("transacoes_horario_exato", (int) transacoesHorarioExato);
        features.put("data_hora_artificial", dataHoraArtificial);
        features.put("variedade_minutos", distribuicaoMinutos.size());
        features.put("variedade_segundos", distribuicaoSegundos.size());
        
        return features;
    }
    
    /**
     * Calcula score de risco temporal baseado em todas as features
     */
    private int calcularScoreRiscoTemporal(Map<String, Object> features) {
        int risco = 0;
        
        // Penalizações por horários suspeitos
        double percMadrugada = (Double) features.getOrDefault("perc_madrugada", 0.0);
        risco += (int) (percMadrugada * 2); // até 200 pontos
        
        // Penalizações por velocidade
        int transacoesRapidas = (Integer) features.getOrDefault("transacoes_rapidas_1min", 0);
        risco += transacoesRapidas * 10; // 10 pontos por transação rápida
        
        int rajadas = (Integer) features.getOrDefault("rajadas_suspeitas", 0);
        risco += rajadas * 15; // 15 pontos por rajada
        
        // Penalizações por padrões robotizados
        Boolean padraoSuspeito = (Boolean) features.getOrDefault("padrao_minutos_suspeito", false);
        if (Boolean.TRUE.equals(padraoSuspeito)) risco += 25;
        
        Boolean dataArtificial = (Boolean) features.getOrDefault("data_hora_artificial", false);
        if (Boolean.TRUE.equals(dataArtificial)) risco += 20;
        
        // Penalizações por baixa variedade temporal
        Double entropiaHorarios = (Double) features.getOrDefault("entropia_horarios", 1.0);
        if (entropiaHorarios < 0.5) risco += 15; // Muito concentrado em poucos horários
        
        // Bonificações por padrões normais
        double percComerciais = (Double) features.getOrDefault("perc_comerciais", 0.0);
        risco -= (int) (percComerciais * 0.5); // Reduz risco para horários comerciais
        
        return Math.max(0, Math.min(100, risco));
    }
    
    // Métodos auxiliares
    private double calcularEntropia(Map<?, Long> distribuicao, int total) {
        return distribuicao.values().stream()
                .mapToDouble(count -> {
                    double p = (double) count / total;
                    return p > 0 ? -p * Math.log(p) / Math.log(2) : 0;
                })
                .sum();
    }
    
    private int detectarRajadas(List<Long> intervalos) {
        int rajadas = 0;
        int sequenciaRapida = 0;
        
        for (Long intervalo : intervalos) {
            if (intervalo <= 30) { // 30 segundos
                sequenciaRapida++;
            } else {
                if (sequenciaRapida >= 3) { // 3+ transações em 30s = rajada
                    rajadas++;
                }
                sequenciaRapida = 0;
            }
        }
        
        // Verificar última sequência
        if (sequenciaRapida >= 3) rajadas++;
        
        return rajadas;
    }
    
    private Map<String, Object> calcularFrequenciaPorHora(List<Map<String, Object>> transacoes) {
        Map<Integer, Long> distribuicao = transacoes.stream()
                .map(t -> ((LocalDateTime) t.get("data_transacao")).getHour())
                .collect(Collectors.groupingBy(h -> h, Collectors.counting()));
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("hora_pico", distribuicao.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(12));
        resultado.put("distribuicao", distribuicao);
        
        return resultado;
    }
    
    private boolean isFeriado(LocalDateTime data) {
        String diasMes = String.format("%02d-%02d", data.getMonthValue(), data.getDayOfMonth());
        return FERIADOS_2025.contains(diasMes);
    }
    
    private Map<String, Object> getFeaturesTemporaisVazias() {
        Map<String, Object> features = new HashMap<>();
        
        // Features de horário
        features.put("horario_predominante", 12);
        features.put("media_horario", 12.0);
        features.put("desvio_horario", 0.0);
        features.put("transacoes_madrugada", 0);
        features.put("transacoes_comerciais", 0);
        features.put("transacoes_noturnas", 0);
        features.put("perc_madrugada", 0.0);
        features.put("perc_comerciais", 0.0);
        features.put("perc_noturnas", 0.0);
        
        // Features de dias
        features.put("transacoes_fins_semana", 0);
        features.put("transacoes_dias_uteis", 0);
        features.put("perc_fins_semana", 0.0);
        features.put("perc_dias_uteis", 0.0);
        
        // Features de velocidade
        features.put("intervalo_medio_segundos", 0);
        features.put("transacoes_rapidas_1min", 0);
        features.put("transacoes_rapidas_5min", 0);
        features.put("rajadas_suspeitas", 0);
        
        // Score
        features.put("score_risco_temporal", 0);
        
        return features;
    }
} 