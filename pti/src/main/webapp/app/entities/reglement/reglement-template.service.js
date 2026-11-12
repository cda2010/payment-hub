(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ReglementTemplate', ReglementTemplate);

    ReglementTemplate.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function ReglementTemplate ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  'api/reglements/reglementTemplate/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
          
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
                   
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    
                    return angular.toJson(copy);
                }
            }
        });
    }
})();
