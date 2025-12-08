package com.ruleengine.benchmarks;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.operator.ComparisonOperator;
import com.ruleengine.domain.rule.Condition;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleMetadata;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import com.ruleengine.infrastructure.strategy.expression.SpelExpressionEngine;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for rule validation performance.
 * 
 * Module: rule-engine-benchmarks
 * Layer: Benchmarks
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class RuleValidationBenchmark {

    private Rule rule;
    private EvaluationContext context;
    private ExpressionEvaluationStrategy strategy;

    @Setup
    public void setup() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Attribute totalAttr = new Attribute("order.total", AttributeType.DECIMAL);
        
        Condition ageCondition = Condition.attributeVsValue("cond-1", "Age check", ageAttr, ComparisonOperator.GTE, 18);
        Condition totalCondition = Condition.attributeVsValue("cond-2", "Total check", totalAttr, ComparisonOperator.GT, 100.0);
        
        rule = new Rule("rule-1", "Test rule", List.of("cond-1", "cond-2"), RuleMetadata.defaults());
        context = EvaluationContext.from(Map.of("customer.age", 25, "order.total", 150.0));
        strategy = new SpelExpressionEngine();
    }

    @Benchmark
    public void benchmarkRuleValidation() {
        List<Condition> conditions = List.of(
            Condition.attributeVsValue("cond-1", "Age check", 
                new Attribute("customer.age", AttributeType.NUMBER), ComparisonOperator.GTE, 18),
            Condition.attributeVsValue("cond-2", "Total check", 
                new Attribute("order.total", AttributeType.DECIMAL), ComparisonOperator.GT, 100.0)
        );
        rule.validate(context, strategy, conditions);
    }
}

