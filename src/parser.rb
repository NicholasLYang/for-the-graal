require 'sxp'

def parse_program(program)
  cst = SXP.read program

  desugar_operator cst
end

def desugar_operator(cst)
  op = cst[0]

  if op == :+ || op == :- || op == :/ || op == :*
    # If we have a binary op with more than two args
    # we convert it to a nested form with the same binary
    # op
    if cst.length > 3
      cst[2] = [cst[0], *cst[2..-1]]
      desugar_operator(cst[2])
    end
    cst.take(3)
  else
    cst[1..-1].each_with_index do |e, i|
      if e.is_a?(Array)
        cst[i + 1] = desugar_operator(e)
      end
    end
    cst
  end
end


p parse_program "(let a (+ 10 11 12))"
