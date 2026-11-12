(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TableauRepartitionCommission', TableauRepartitionCommission);

    TableauRepartitionCommission.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function TableauRepartitionCommission ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'tableau-repartition-commissions/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByGroup': { method: 'GET',url:BASE_URL_API+'tableau-repartition-commissions/group/:groupId', isArray: true},
            'queryByGroupOptimise': { method: 'GET',url:BASE_URL_API+'tableau-repartition-commissions-optimise/group/:groupId', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateDebut = DateUtils.convertLocalDateFromServer(data.dateDebut);
                        data.dateFin = DateUtils.convertLocalDateFromServer(data.dateFin);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateDebut = DateUtils.convertLocalDateToServer(copy.dateDebut);
                    copy.dateFin = DateUtils.convertLocalDateToServer(copy.dateFin);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateDebut = DateUtils.convertLocalDateToServer(copy.dateDebut);
                    copy.dateFin = DateUtils.convertLocalDateToServer(copy.dateFin);
                    return angular.toJson(copy);
                }
            }
        });
    }
})();
