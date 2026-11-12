(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('BilanOperation', BilanOperation);

    BilanOperation.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function BilanOperation ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'bilan-operations/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateDerniereOperation = DateUtils.convertLocalDateFromServer(data.dateDerniereOperation);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateDerniereOperation = DateUtils.convertLocalDateToServer(copy.dateDerniereOperation);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateDerniereOperation = DateUtils.convertLocalDateToServer(copy.dateDerniereOperation);
                    return angular.toJson(copy);
                }
            }
        });
    }
})();
