(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ParamExecutionJob', ParamExecutionJob);

    ParamExecutionJob.$inject = ['$resource','BASE_URL_API'];

    function ParamExecutionJob ($resource,BASE_URL_API) {
        var resourceUrl =  'api/param-execution-jobs/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByPtiJobDetail':{method: 'GET',url:BASE_URL_API+'queryByPtiJobDetail' , isArray: true},
            'countByKeys': {method: 'GET', url: BASE_URL_API+'countByKeys', isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
