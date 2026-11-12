(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ConfigPublicite', ConfigPublicite);

    ConfigPublicite.$inject = ['$resource'];

    function ConfigPublicite ($resource) {
        var resourceUrl = 'api/publicite/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true },
            'queryAll': { method: 'GET', url: 'api/publicite', isArray: true },
            'getImage': { method: 'GET', url: 'api/publicite/image/:id', responseType: 'blob' },
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },
            'save': {
                method: 'POST',
                transformRequest: function(data) {
                var formData = new FormData();
                var dateDebut = new Date(data.dateDebut);
                var formattedDateDebut = dateDebut.toISOString().split('T')[0];
                formData.append('dateDebut', formattedDateDebut);
                var dateFin = new Date(data.dateFin);
                var formattedDateFin = dateFin.toISOString().split('T')[0];
                formData.append('dateFin', formattedDateFin);
                formData.append('pays', data.pays.id);
                formData.append('frequence', data.frequence);
                if (data.urlMinioPublicite && data.urlMinioPublicite[0]) {
                    formData.append('urlMinioPublicite', data.urlMinioPublicite[0]);
                }
                //return angular.toJson(formData);
                    return formData;
                },
                headers: { 'Content-Type': undefined }
            }
        });
    }
})();
