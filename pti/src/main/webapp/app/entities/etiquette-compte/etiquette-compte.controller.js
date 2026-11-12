(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtiquetteCompteController', EtiquetteCompteController);

    EtiquetteCompteController.$inject = ['$scope', 'InitCrud', '$state', 'EtiquetteCompte', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function EtiquetteCompteController($scope, InitCrud, $state, EtiquetteCompte, ParseLinks, AlertService, paginationConstants, pagingParams) {
        var vm = this;
        vm.title = "Liste des etiquettes comptes";

        vm.columns = [{
            name: 'code',
            title: 'Code'
        },{
            name: 'obligatoire',
            title: 'Obligatoire',
            type:'Boolean'
        },{
            name: 'transversal',
            title: 'Transversal',
            type:'Boolean'
        },
        {
            name: 'estDebiteur',
            title: 'Débiteur?',
            type:'Boolean'
        }
            ,{
            name: 'niveau',
            title: 'Niveau',
            type:'Niveau'
        },{
            name: 'libelle',
            title: 'Libellé'
           
        }];

        vm.actions = {
            view: false,
            edit: true,
            delete: true
        };

        var datas = {
            vm: vm,
            entityDatas: {
                code: null,
                id: null,
                 libelle: null
            },
            entity: EtiquetteCompte,
            addTemplateUrl: "etiquette-compte/etiquette-compte-dialog.html",
            deleteTemplateUrl: "etiquette-compte/etiquette-compte-delete-dialog.html",
            controller: "EtiquetteCompte",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
