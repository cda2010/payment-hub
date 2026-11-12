(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VilleController', VilleController);

    VilleController.$inject = ['Ville', 'InitCrud','$scope'];

    function VilleController(Ville, InitCrud,$scope) {

        var vm = this;
        vm.title = "Liste des villes";
        vm.filtrer=true;
        vm.doFiltre=false;
        vm.itemSearch = {
            "adresse": null
        };
        vm.columns = [{
            name: 'nom',
            title: 'Nom'
        }, {
            name: 'libellePays',
            title: 'Pays'
        }];
        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        var datas = {
            vm: vm,
            entityDatas: {
                nom: null,
                paysId:null,
                id: null
            },
            entity: Ville,
            addTemplateUrl: "ville/ville-dialog.html",
            deleteTemplateUrl: "ville/ville-delete-dialog.html",
            controller: "Ville",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
