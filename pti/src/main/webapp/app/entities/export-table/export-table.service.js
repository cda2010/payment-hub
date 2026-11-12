(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ExportTable', ExportTable);

    ExportTable.$inject = ['$resource', 'BASE_URL_API', 'DateUtils',];

    function ExportTable ($resource, BASE_URL_API, DateUtils) {
        var resourceUrl =  'api/export-tables/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryAll': { method: 'GET',url: BASE_URL_API+'/export-tables-all', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateGeneration = DateUtils.convertLocalDateToServer(copy.dateGeneration);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateGeneration = DateUtils.convertLocalDateToServer(copy.dateGeneration);
                    return angular.toJson(copy);
                }
            }
        });
    }
})();
