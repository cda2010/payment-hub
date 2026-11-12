(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('Operation', Operation);

    Operation.$inject = ['$resource', 'DateUtils','URL_PRESENTATION'];

    function Operation ($resource, DateUtils,URL_PRESENTATION) {
        var resourceUrl ='api/operations/:id';

        return $resource(resourceUrl, {}, {
                'queryToHist': { method: 'GET', url:'api/historiqueAppelTop10', isArray: true},
                'queryToAllHist': { method: 'GET', url:'api/historiqueAppelAll', isArray: true},//l'ensemble des appels 

                'queryToDayAllHist': { method: 'GET', url:'api/historiqueAppelForDay', isArray: true},//l'esnsemble des appels du jour
            
                'apiTotalPerCodeErr': { method: 'GET', url:'api/apiTotalPerCodeErr', isArray: true},
                'queryNombreHist': { method: 'GET', url:'api/apiTotalRequest', isArray: true},
                'queryLigne': { method: 'GET', url:'api/ligne-operations/oper/:id', isArray: true},
                'query': { method: 'GET', isArray: true},
                'operationTop10Filtered': { method: 'GET', url: 'api/operations/top10', isArray: true},
                'operationFiltered': { method: 'GET', url: 'api/operations/filtered', isArray: true},
         
                'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateOperation = DateUtils.convertDateTimeFromServer(data.dateOperation);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
