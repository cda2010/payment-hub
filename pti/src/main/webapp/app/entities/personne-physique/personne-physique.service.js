(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('PersonnePhysique', PersonnePhysique);

    PersonnePhysique.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function PersonnePhysique ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'personne-physiques/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: false},
            'get': {
                method: 'GET',
                isArray: false,
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateNaissance = DateUtils.convertLocalDateFromServer(data.dateNaissance);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateNaissance = DateUtils.convertLocalDateToServer(copy.dateNaissance);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateNaissance = DateUtils.convertLocalDateToServer(copy.dateNaissance);
                    return angular.toJson(copy);
                }
            }
        });
    }
})();
