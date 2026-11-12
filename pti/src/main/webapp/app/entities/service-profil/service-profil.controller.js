(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ServiceProfilController', ServiceProfilController);

    ServiceProfilController.$inject = ['$state', 'ServiceProfil','InitCrud','$scope'];

    function ServiceProfilController($state, ServiceProfil,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des profils par services";
        vm.columns = [{
            name: 'nom',
            title: 'Nom'
            },
            {
                name: 'typeProfilService',
                title: 'Type'
            },
            {
                name: 'description',
                title: 'Description'
            }
        ];
        vm.actions = {
            view: true,
            edit: true,
            delete: true
        };

        var datas = {
            vm: vm,
            entityDatas: {
                modeCalcul: null,
                appliqueSur: null,
                valeur: null,
                id: null
            },
            entity: ServiceProfil,
            addTemplateUrl: "service-profil/service-profil-dialog.html",
            deleteTemplateUrl: "service-profil/service-profil-delete-dialog.html",
            controller: "ServiceProfil",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
