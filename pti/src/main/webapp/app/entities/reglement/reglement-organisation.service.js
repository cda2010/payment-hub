(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ReglementOrganisation', ReglementOrganisation);

    ReglementOrganisation.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function ReglementOrganisation ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  'api/reglements/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getLigneReglement': {method: 'GET', url: BASE_URL_API+'getligneReglement',isArray: true},
            'validerReglement': {method: 'POST', url: BASE_URL_API+'operations/valider-reglement-compte/:reglementId',isArray: false},
            'annulationReglement': {method: 'GET', url: BASE_URL_API+ 'operation/annulationReglement', isArray: true},
            'detacherReglement': {method: 'GET', url: BASE_URL_API+ 'operation/detacherReglement', isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateReglement = DateUtils.convertLocalDateFromServer(data.dateReglement);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateReglement = DateUtils.convertLocalDateToServer(copy.dateReglement);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateReglement = DateUtils.convertLocalDateToServer(copy.dateReglement);
                    return angular.toJson(copy);
                }
            }
        });
    }
})();
