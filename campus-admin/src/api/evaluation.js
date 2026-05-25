import request from './request'

export function getEvaluationList(params) {
  return request({
    url: '/api/evaluation/admin/list',
    method: 'GET',
    params
  })
}

export function deleteEvaluation(evaluationId) {
  return request({
    url: `/api/evaluation/admin/delete/${evaluationId}`,
    method: 'DELETE'
  })
}

export function replyEvaluation(evaluationId, data) {
  return request({
    url: `/api/evaluation/admin/reply/${evaluationId}`,
    method: 'POST',
    data
  })
}