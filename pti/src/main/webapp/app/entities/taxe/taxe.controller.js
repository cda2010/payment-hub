(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TaxeController', TaxeController);

    TaxeController.$inject = ['InitCrud','$scope','Taxe'];

    function TaxeController(InitCrud,$scope, Taxe) {
        var vm = this;
        vm.title = "Liste des taxes";
        vm.columns = [{
            name: 'code',
            title: 'Code'
        },{
            name: 'type',
            title: 'Type'
        },{
            name: 'nom',
            title: 'Libelle'
        },{
            name: 'etiquette',
            title: 'Etiquette'
        }];
        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        var datas = {
            vm: vm,
            entityDatas: {
                modeCalcul: null,
                appliqueSur: null,
                valeur: null,
                id: null,
                type:"TAXE"
            },
            entity: Taxe,
            addTemplateUrl: "taxe/taxe-dialog.html",
            deleteTemplateUrl: "taxe/taxe-delete-dialog.html",
            controller: "Taxe",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
