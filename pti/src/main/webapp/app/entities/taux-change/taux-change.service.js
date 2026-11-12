(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TauxChange', TauxChange);

    TauxChange.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function TauxChange ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'taux-changes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateTaux = DateUtils.convertLocalDateFromServer(data.dateTaux);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateTaux = DateUtils.convertLocalDateToServer(copy.dateTaux);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateTaux = DateUtils.convertLocalDateToServer(copy.dateTaux);
                    return angular.toJson(copy);
                }
            }
        });
    }
})();
