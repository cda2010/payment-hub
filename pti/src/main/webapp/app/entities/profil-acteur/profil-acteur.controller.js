(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilActeurController', ProfilActeurController);


    ProfilActeurController.$inject = ['InitCrud', '$scope', 'ProfilActeur'];

    function ProfilActeurController(InitCrud, $scope, ProfilActeur) {
        var vm = this;
        vm.title = "Liste des profils acteurs";
        vm.columns = [{
            name: 'nom',
            title: 'Nom'
        }, {
            name: 'description',
            title: 'Description'
        }, {
            name: 'type',
            title: 'Type'
        },
        {
                name: 'destineA',
                title: 'Destiné à'
        },
            {
            name: 'active',
            title: 'Par defaut',
            type:'Boolean'
        }, {
            name: 'codeFse',
            title: 'Code FSE'
        }, {
            name: 'id',
            title: '#ID'
        }];
        vm.actions = {
            view: true,
            edit: true,
            delete: true
        };

        var datas = {
            vm: vm,
            entityDatas: {
                nom: null,
                description: null,
                codeFse:null,
                id: null,
                tableauFrais:[],
                tableauCumuls:[],
                tableauRepartitionCommissions:[],
                groupRepartitions:[]
            },
            entity: ProfilActeur,
            addTemplateUrl: "profil-acteur/profil-acteur-dialog.html",
            deleteTemplateUrl: "profil-acteur/profil-acteur-delete-dialog.html",
            controller: "ProfilActeur",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "lg",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();